# MedFlow AI 프로젝트 가이드

> 기준일: 2026-08-05
> 근거: `backend/src/main`, `backend/src/test`, `backend/build.gradle`, 애플리케이션 설정 및 `docs/adr`  
> 원칙: 이 문서의 **구현 완료**는 현재 소스 코드에 실행 경로가 존재한다는 뜻이다. 테스트 전체 통과나 운영 배포 완료를 뜻하지 않는다.

## 1. 프로젝트 개요

MedFlow AI는 환자, 의사, 관리자가 병원 진료 예약과 사전 문진을 관리하는 Spring Boot 백엔드 애플리케이션이다. 현재 저장소에는 다음 백엔드 기능이 구현되어 있다.

- JWT 기반 회원가입, 로그인, 토큰 재발급, 로그아웃, 탈퇴
- 환자 프로필과 병원 정보 관리
- 의사 자격 신청 및 관리자 승인/반려
- 의사 진료 스케줄과 환자 예약 관리
- 예약 기반 문진 작성과 AI 분석
- 환자, 담당 의사, 관리자의 역할별 조회/상태 변경

단일 Gradle 모듈의 모듈러 모놀리스이며 프론트엔드 소스는 현재 저장소에 없다.

## 2. 기술 스택

| 영역 | 실제 적용 기술 |
|---|---|
| 언어 | Java 21 |
| 애플리케이션 | Spring Boot 3.5.16 |
| Web | Spring MVC, Jakarta Bean Validation |
| 인증/인가 | Spring Security, JWT(JJWT 0.11.5), BCrypt |
| ORM | Spring Data JPA, Hibernate |
| 동적 조회 | QueryDSL 5.1.0 Jakarta |
| 운영 DB 설정 | MySQL Connector/J, MySQL 8.4 Docker 이미지 |
| AI | Google Gen AI SDK 1.64.0, Gemini 또는 Fake 구현체 |
| API 문서 | springdoc-openapi 2.8.16, Swagger UI |
| 빌드 | Gradle Wrapper |
| 코드 생성 | Lombok, QueryDSL annotation processor |
| 테스트 | JUnit Platform, Spring Boot Test, Spring Security Test, Mockito |

`docker-compose.yml`에 Redis 7이 정의되어 있지만 Redis 라이브러리와 사용 코드는 없다.

## 3. 사용자 역할

`com.medflow.user.entity.UserRole`에 다음 역할이 있다.

| 역할 | 코드상 용도 |
|---|---|
| `PATIENT` | 환자 예약 생성/조회/취소, 문진 생성/조회/수정, 본인 분석 조회 |
| `DOCTOR` | 의사 신청, 담당 예약 조회 및 상태 변경, 담당 문진 분석 조회 |
| `ADMIN` | 사용자/환자/병원/의사/예약 관리 조회 및 의사 승인 |

회원가입 API는 `ADMIN`을 거부하고 요청된 `PATIENT` 또는 `DOCTOR` 역할로 User를 생성한다. 개발 프로필의 `DevDataInitializer`는 관리자 계정을 별도로 생성한다.

역할과 프로필은 별도다. User가 `PATIENT` 역할이어도 Patient 프로필은 `/api/v1/patient/create` 호출 전까지 존재하지 않을 수 있고, `DOCTOR` 역할이어도 Doctor 신청 전에는 Doctor 엔티티가 없다.

## 4. 핵심 도메인과 책임

| 도메인 | 주요 클래스 | 책임 |
|---|---|---|
| 사용자 | `User`, `UserService` | 계정 역할/상태, 관리자 사용자 조회 |
| 인증 | `AuthService`, `JwtGenerator`, `JwtProvider` | 인증, JWT와 Refresh Token 수명주기 |
| 토큰 | `RefreshToken` | DB 저장 Refresh Token과 만료 시각 |
| 환자 | `Patient`, `PatientServiceImpl` | 사용자와 연결된 환자 프로필 |
| 병원 | `Hospital`, `HospitalServiceImpl` | 병원 정보, 운영 상태, 공개 통합 검색 |
| 의사 | `Doctor`, `DoctorService`, `DoctorAdminService` | 자격 신청, 공개 프로필, 수정, 취소, 승인/반려 |
| 스케줄 | `DoctorSchedule`, `DoctorScheduleService` | 진료 슬롯 생성과 예약 가능 상태 |
| 예약 | `Reservation`, 예약 서비스 3종 | 환자/의사/관리자 관점 예약 유스케이스 |
| 문진 | `Questionnaire`, `QuestionnaireService` | 예약별 사전 문진 생성/조회/수정 |
| 분석 | `QuestionnaireAnalysis`, 분석 서비스/어댑터 | 문진 분석 실행, 결과 및 상태 저장 |

## 5. 패키지 구조와 역할

```text
com.medflow
├── auth
│   ├── controller, dto, jwt, security, service
├── token
│   ├── entity, repository
├── user
│   ├── controller, dto, entity, repository, service
├── patient
│   ├── controller, dto, entity, repository, service
├── hospital
│   ├── controller, dto, entity, repository, service
├── doctor
│   ├── controller, dto, entity, repository, service
├── reservation
│   ├── controller, dto, entity, repository, service
├── questionnaire
│   ├── analysis, controller, dto, entity, event, repository, service
└── common
    ├── config, entity, exception, init, response, security
```

- `controller`: HTTP 경로, 요청 검증, 인증 사용자 추출, `ApiResponse` 포장
- `dto`: API 요청/응답 모델 및 Entity→Response 변환
- `service`: 트랜잭션 경계, 유스케이스, 소유권/상태 검증
- `entity`: 영속 상태와 도메인 상태 전이
- `repository`: Spring Data JPA 및 QueryDSL 조회
- `analysis`: AI 공급자 인터페이스와 Gemini/Fake 구현
- `event`: 문진 저장 커밋 이후 분석 시작 이벤트
- `common`: 공통 설정, 응답, 예외, 감사 필드

## 6. 구현 완료 기능

### 인증과 사용자

- 이메일 중복 검사 및 역할별 프로필 통합 회원가입(`PATIENT`, `DOCTOR`; `ADMIN` 거부)
- BCrypt 비밀번호 인증
- Access Token 60분, Refresh Token 14일 발급
- 사용자별 Refresh Token 저장/갱신, 재발급 시 rotation
- 로그아웃 시 요청된 Refresh Token 삭제
- 회원 탈퇴 시 연결 Patient를 필수 조회한 뒤 User `WITHDRAWN` 및 User/Patient `deletedAt` 기록, Refresh Token 삭제
- JWT 필터 인증과 `@PreAuthorize` 인가
- 관리자 사용자 단건/전체 조회

### 환자, 병원, 의사

- 환자 프로필 생성/본인 조회/수정, 관리자 전체 조회
- 활성 병원 목록/상세 조회 및 병원명·지역·주소 통합 검색
- 관리자 병원 등록/목록/수정/폐쇄 및 soft delete
- 의사 자격 신청/프로필 조회/대기 상태 수정/신청 취소
- 의사 전문과목·소개·연락처 공개 프로필 조회 및 수정
- 관리자 의사 상태별 목록/상세/승인/반려
- 승인된 의사의 진료 슬롯 생성과 가용 슬롯 조회

### 예약

- 환자의 가용 스케줄 예약 요청
- 환자 예약 조건 검색 및 페이징, 본인 예약 취소
- 의사의 담당 예약 전체/오늘/날짜별/조건 검색
- 담당 예약 환자 정보 조회
- 예약 승인, 거절, 진료 완료
- 관리자 전체 예약 조건 검색 및 페이징

### 문진과 AI

- 본인 예약에 예약당 1개의 문진 작성
- 본인 문진 조회와 진료 시작 전 수정
- 취소/완료 예약의 문진 작성/수정 제한
- 문진 생성/수정 커밋 후 분석 요청
- Gemini 구조화 JSON 분석 또는 Fake 분석
- 분석 상태 `PENDING → PROCESSING → COMPLETED/FAILED`
- 환자 본인 및 담당 의사의 분석 결과 조회

## 7. 미구현 또는 향후 기능

다음은 코드에 구현되어 있지 않다. 기존 ADR 주석 또는 구성 흔적이 있더라도 완료 기능으로 보지 않는다.

- 프론트엔드 애플리케이션
- 진료 기록/의무 기록 도메인
- 알림 도메인과 발송 채널
- 결제 기능
- 구조화된 진료과 Department 도메인(현재 Doctor에는 자유 입력 전문과목만 존재)
- Redis 연동
- 단일 응답으로 예약·환자·문진·AI 결과를 묶는 의사용 통합 조회 API
- AI 작업 큐 또는 `@Async` 기반 비동기 실행
- AI 실패 수동 재시도 API
- DB 마이그레이션 도구와 버전 관리 스크립트
- Soft Delete 전역 조회 필터/복구 API

`docs/PROJECT_STATUS.md`에는 개선 후보와 제안 로드맵이 별도 정리되어 있다. 해당 내용은 구현 완료 상태가 아니다.

## 8. 인증 및 권한 정책

### HTTP 보안

`SecurityConfig`는 CSRF, form login, HTTP Basic, 세션을 비활성화하고 `STATELESS`로 동작한다.

공개 경로:

- `/`
- `/api/v1/auth/signup`
- `/api/v1/auth/login`
- `/swagger-ui/**`
- `/v3/api-docs/**`

그 외 경로는 유효한 Access Token 인증이 필요하다. 이 설정 때문에 `/api/v1/auth/reissue`도 현재 Access Token 인증이 필요하다.

### JWT 처리

- `Authorization: Bearer {token}`에서 토큰을 추출한다.
- 서명/형식/만료를 검증한다.
- Access Token subject의 이메일로 User를 DB 조회한다.
- User 역할을 `ROLE_{role}` 권한으로 SecurityContext에 저장한다.
- 잠긴 계정은 `isAccountNonLocked=false`, 비활성 계정은 `isEnabled=false`다.

### 메서드 권한과 소유권

- 환자 예약/문진 컨트롤러: 클래스 수준 `PATIENT`
- 의사 예약/문진 컨트롤러: 클래스 수준 `DOCTOR`
- 관리자 의사/병원/예약: 클래스 수준 `ADMIN`
- 사용자/환자 관리자 조회: 메서드 수준 `ADMIN`
- 환자 프로필 생성/조회/수정과 일반 병원 조회는 역할 제한 없이 인증만 필요하다.
- 예약, 문진, 분석은 Service에서 Patient/Doctor 연관관계로 본인 또는 담당 데이터인지 검증한다.

## 9. AI 문진 분석 흐름

1. `QuestionnaireService`가 환자 및 예약 소유권과 상태를 검증한다.
2. `Questionnaire`와 `PENDING` 상태의 `QuestionnaireAnalysis`를 저장한다.
3. `QuestionnaireAnalysisRequestedEvent`를 발행한다.
4. `QuestionnaireAnalysisEventListener`가 `AFTER_COMMIT`에 이벤트를 받는다.
5. `QuestionnaireAnalysisService.analyze`가 `REQUIRES_NEW` 트랜잭션으로 분석을 시작한다.
6. `ai.provider=gemini`이면 `GeminiQuestionnaireAnalyzer`, `fake`이면 `FakeQuestionnaireAnalyzer`를 사용한다.
7. Gemini 구현은 진단/처방 금지 지침과 JSON schema를 사용한다.
8. 성공하면 요약/핵심 내용/주의 정보/의사 확인 항목/우선순위를 저장하고 `COMPLETED`로 바꾼다.
9. 예외가 발생하면 결과를 지우고 `FAILED`로 바꾼다.

이 이벤트는 커밋 이후 별도 트랜잭션이지만 `@Async`가 없으므로 호출 스레드 관점에서는 비동기가 아니다.

## 10. 개발 및 코딩 규칙

저장소 루트 `AGENTS.md`를 우선 적용한다. 현재 핵심 규칙은 다음과 같다.

- 기능별 패키지와 기존 설계를 유지하고 최소 범위로 수정한다.
- Entity를 Controller에서 직접 반환하지 않고 Request/Response DTO를 사용한다.
- 상태 변경은 Entity 도메인 메서드로 구현하고 Setter를 최소화한다.
- 변경은 하나의 `@Transactional`, 조회는 `@Transactional(readOnly = true)`를 사용한다.
- Spring Data JPA를 우선하고 복잡한 동적 조회는 QueryDSL을 사용한다.
- 로그인 사용자 ID는 `AuthenticationPrincipal`에서 가져오며 요청 값으로 받지 않는다.
- 본인 데이터와 담당 데이터의 소유권을 검증한다.
- 기존 `BusinessException`과 `ErrorCode`를 재사용한다.
- 공통 `ApiResponse`와 적절한 HTTP Status를 사용한다.
- Lazy Loading을 기본으로 하며 N+1이 예상되면 fetch join/EntityGraph를 검토한다.
- BOM 없는 UTF-8과 기존 스타일의 한글 주석을 사용한다.
- 기능 구현 시 정상, 권한, 예외, 상태 변경, 미존재 데이터 테스트를 작성한다.
- 구조 변경 시 `PROJECT_GUIDE.md`, API 추가 시 `API.md`, ERD 변경 시 `ERD.md`를 갱신한다.

## 11. 현재 프로젝트 진행 상태

- 핵심 백엔드 도메인과 44개 Controller API가 구현되어 있다.
- 예약 및 문진 중심의 서비스/컨트롤러/AI 이벤트 테스트가 있다.
- 최근 전체 테스트 실행 결과는 110개 중 106개 통과, 4개 실패다.
  - `BackendApplicationTests.contextLoads`: Hibernate dialect 결정 실패
  - `DoctorQuestionnairesServiceTest`: Mockito stubbing 관련 3건 실패
- 따라서 현재 코드는 기능 구현 단계이나 전체 테스트 통과 기준선은 아니다.
- Swagger UI와 OpenAPI 경로는 구성되어 있으나 API별 상세 annotation은 제한적이다.
- 기본 설정에서 `spring.profiles.active=dev`, Hibernate `ddl-auto=update`, SQL debug logging을 사용한다.

## 코드로 확인한 내용

- Java/Spring/DB/AI/보안 라이브러리 버전과 실행 설정
- 세 사용자 역할과 Controller/Service의 실제 권한 및 소유권 검사
- 사용자, 환자, 병원, 의사, 스케줄, 예약, 문진, AI 분석 기능
- 기능 기반 패키지와 계층별 책임
- Gemini/Fake 조건부 구현과 커밋 후 분석 흐름
- 현재 미구현 도메인 및 주석 상태 코드
- 전체 테스트 110개 중 4개 실패 상태

## 작성자 확인이 필요한 내용

- [작성자 확인 필요: 서비스의 공식 제품 범위와 대상 사용자/기관]
- [작성자 확인 필요: 운영 환경에서 사용할 `ai.provider`와 Gemini 모델]
- [작성자 확인 필요: `/api/v1/auth/reissue`가 Access Token 인증을 요구하는 정책이 의도된 것인지]
- [작성자 확인 필요: 환자 프로필 API가 모든 인증 역할에 열려 있는 것이 의도된 것인지]
- [작성자 확인 필요: Docker Compose의 Redis 도입 목적과 예정 시점]
- [작성자 확인 필요: 의사용 통합 조회가 단일 API를 의미하는지 클라이언트 조합을 의미하는지]
- [작성자 확인 필요: 운영 배포 방식, CI/CD, 모니터링 및 로그 보존 정책]
