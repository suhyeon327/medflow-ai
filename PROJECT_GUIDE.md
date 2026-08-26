# MedFlow 프로젝트 가이드

> 기준일: 2026-08-20  
> 이 문서는 프로젝트 전체의 기준 문서다. 설명과 코드가 다르면 현재 `backend/src`, `frontend/src`, 빌드 및 배포 설정을 우선한다.

## 1. 프로젝트 목적

MedFlow는 병원·의사 탐색, 진료 시간 예약, 진료 전 문진, 의료진용 AI 문진 요약을 하나의 흐름으로 연결하는 역할 기반 웹 애플리케이션이다. 환자는 예약 전에 증상 정보를 구조화해 전달하고, 의사는 담당 예약과 AI가 정리한 문진 정보를 확인하며, 관리자는 병원과 의사 승인 및 서비스 현황을 관리한다.

이 프로젝트가 해결하려는 핵심 문제는 다음과 같다.

- 병원과 승인된 의사, 예약 가능 시간을 여러 경로에서 따로 확인해야 하는 불편
- 진료 직전에 문진을 다시 수집하느라 발생하는 정보 누락과 시간 소모
- 긴 자유 서술형 문진에서 핵심 내용과 확인 항목을 빠르게 파악하기 어려운 문제
- 환자·의사·관리자 기능을 같은 서비스 안에서 안전하게 분리해야 하는 문제

AI 분석은 진단이나 처방을 대신하지 않는다. 문진 원문을 의료진이 확인하기 쉬운 요약, 핵심 내용, 주의 신호, 추가 확인 항목으로 구조화하는 보조 기능이다.

## 2. 주요 사용자

| 역할 | 목적 | 대표 기능 |
| --- | --- | --- |
| `PATIENT` | 진료 탐색과 예약, 사전 정보 전달 | 병원·의사 조회, 시간 예약, 예약 조회·취소, 문진 작성·수정, AI 분석 확인, 프로필 관리 |
| `DOCTOR` | 진료 일정과 담당 예약 관리 | 의사 프로필 관리, 슬롯 생성·조회, 담당 예약 및 환자 정보 조회, 담당 문진 AI 분석 확인 |
| `ADMIN` | 서비스 기준 정보와 가입 의사 관리 | 병원 등록·수정·종료, 의사 승인·반려, 사용자·예약 조회, 관리자 대시보드 |

로그인하지 않은 사용자는 활성 병원, 승인되고 활성 계정에 연결된 의사, 예약 가능 슬롯을 조회할 수 있다. 상세 구현 상태는 [REQUIREMENTS.md](REQUIREMENTS.md)를 따른다.

## 3. 핵심 기능

1. `PATIENT`와 `DOCTOR` 역할별 회원가입 및 JWT 로그인
2. 공개 병원 검색, 병원별 의사 조회, 의사 상세 및 예약 가능 슬롯 조회
3. 관리자 의사 승인 후 의사의 진료 슬롯 생성
4. 환자의 슬롯 기반 예약과 예약 내역 필터링·취소
5. 예약당 하나의 문진 작성·조회·수정
6. Gemini 또는 테스트용 Fake 분석기를 이용한 문진 구조화
7. 의사의 담당 예약·환자·문진 분석 조회
8. 관리자의 병원, 의사, 사용자, 예약 조회와 관리 화면
9. 담당 의사가 `APPROVED` 예약을 직접 `COMPLETED`로 변경하는 진료 완료 처리

## 4. 기술 스택

| 영역 | 실제 사용 기술 |
| --- | --- |
| Backend | Java 21, Spring Boot 3.5.16, Gradle, Spring MVC, Spring Validation |
| Security | Spring Security, JJWT 0.11.5, BCrypt, Stateless JWT |
| Persistence | Spring Data JPA, Hibernate, QueryDSL 5.1.0, MySQL Connector |
| API 문서 | springdoc-openapi 2.8.16, Swagger UI |
| AI | Google Gen AI SDK 1.64.0, Gemini API, 프로파일용 Fake analyzer |
| Frontend | React 19, TypeScript 5.6, Vite 6, React Router 8 |
| Frontend data | Axios, TanStack React Query 5 |
| UI | Tailwind CSS 3 |
| Database | MySQL 8.4(Docker Compose 기준) |
| 배포/자동화 | Docker multi-stage build, Nginx, Docker Compose, GitHub Actions, EC2 SSH 배포 |

Docker Compose에는 Redis 7 컨테이너와 환경변수가 선언되어 있으나, 현재 백엔드에는 Redis 의존성이나 사용 코드가 없다. Redis를 현재 기술 스택의 애플리케이션 구성 요소로 간주하지 않는다.

## 5. 주요 도메인

- `user`: 이메일, 암호화된 비밀번호, 역할, 계정 상태를 가진 인증 주체
- `patient`: 환자 역할의 이름, 생년월일, 성별, 전화번호 프로필
- `doctor`: 사용자, 소속 병원, 면허번호, 진료과, 소개, 승인 상태를 연결하는 의사 프로필
- `hospital`: 공개 조회와 관리자 관리의 기준이 되는 병원 정보와 운영 상태
- `doctor_schedule`: 의사가 만든 날짜·시작·종료 시간 단위의 예약 슬롯
- `reservation`: 환자와 의사 슬롯을 연결하고 승인·완료·취소 상태를 관리하는 예약
- `questionnaire`: 예약당 하나만 존재하는 진료 전 문진 원문
- `questionnaire_analysis`: 문진당 하나만 존재하는 AI 분석 상태와 결과
- `refresh_token`: 재발급을 위해 DB에 저장되는 Refresh Token

도메인 관계와 제약은 [ERD.md](ERD.md)에 정리한다.

## 6. 프로젝트 구조

```text
medflow-ai/
├─ backend/
│  ├─ src/main/java/com/medflow/
│  │  ├─ auth/             # 로그인, 회원가입, JWT, 인증 사용자
│  │  ├─ user/             # 사용자와 관리자 사용자 조회
│  │  ├─ patient/          # 환자 프로필
│  │  ├─ doctor/           # 의사 프로필, 승인, 슬롯
│  │  ├─ hospital/         # 공개 병원 조회와 관리자 관리
│  │  ├─ reservation/      # 환자·의사·관리자 예약 흐름과 수동 진료 완료
│  │  ├─ questionnaire/    # 문진, 분석, Gemini 연동
│  │  ├─ token/            # Refresh Token 저장
│  │  └─ common/           # 설정, 보안, 공통 응답·예외, 감사 필드
│  ├─ src/main/resources/  # 공통·dev·prod·baseline 설정
│  └─ src/test/            # 단위, MVC 보안, JPA 테스트
├─ frontend/
│  ├─ src/api/             # Axios 기반 API 함수와 토큰 재발급
│  ├─ src/auth/            # 인증 컨텍스트와 localStorage 토큰 관리
│  ├─ src/features/        # React Query 훅과 기능별 컴포넌트
│  ├─ src/pages/           # 공개·환자·의사·관리자 화면
│  ├─ src/routes/          # 경로와 인증·역할 가드
│  ├─ src/layouts/         # 공개 및 역할별 레이아웃
│  └─ src/types/           # 프론트 API 타입
├─ .github/workflows/ci-cd.yml
├─ docker-compose.yml
└─ README.md
```

백엔드는 도메인별 패키지 안에 Controller, Service, Repository, Entity, DTO를 배치한다. 프론트엔드는 API 함수와 서버 상태 훅을 분리하고 페이지가 기능 훅을 조합하는 구조다.

## 7. 개발 원칙

- 현재 코드와 기존 설계를 우선하고 요청 범위를 벗어난 리팩터링을 하지 않는다.
- Controller는 인증 사용자와 요청을 받아 Service에 위임하고 `ApiResponse`로 감싼 DTO만 반환한다.
- Service는 업무 규칙과 트랜잭션 경계를 담당한다. 조회는 `readOnly = true`를 사용한다.
- Entity 상태 변경은 가능한 한 도메인 메서드(`approve`, `reserve`, `cancel`, `complete` 등)로 수행한다.
- Repository는 Spring Data JPA를 우선하고, 다중 조건·페이징 조회는 QueryDSL을 사용한다.
- 연관관계는 기본적으로 LAZY이며 `spring.jpa.open-in-view=false`다. 필요한 데이터는 트랜잭션 안에서 DTO로 변환하거나 Fetch Join으로 가져온다.
- 운영 코드 변경 시 기존 테스트와 보안 경계를 보존하고, 중복 클래스나 임의 패키지 이동을 만들지 않는다.
- 주석을 추가해야 할 때는 프로젝트 관례에 따라 한글을 사용하고 파일은 BOM 없는 UTF-8로 유지한다.

## 8. API 설계 원칙

- 기본 경로는 `/api/v1`이다.
- 성공과 실패 모두 공통 envelope를 사용한다.

```json
{
  "success": true,
  "data": {},
  "timestamp": "2026-08-20T12:00:00"
}
```

- 실패 시 `data` 대신 `{ "code", "message" }` 형태의 `error`가 포함된다.
- Entity를 응답으로 직접 노출하지 않고 Request/Response DTO를 사용한다.
- 로그인 사용자 ID는 JWT로 만든 `UserPrincipal`에서 가져오며 요청 본문이나 URL로 받지 않는다.
- 목록 규모가 커질 수 있는 공개 병원, 예약, 사용자 API는 페이지 응답을 사용한다. 일부 관리자 목록과 의사 승인 목록은 아직 전체 리스트 응답이다.
- 실제 Endpoint와 DTO는 [API.md](API.md)를 기준으로 한다.

## 9. 인증과 인가

- Spring Security는 세션을 만들지 않는 Stateless 구성이다.
- Access Token은 `Authorization: Bearer <token>` 헤더로 전달하며 유효기간은 60분이다.
- Refresh Token은 14일 유효하며 원문이 DB에 저장된다. 로그인 시 사용자 기준 기존 토큰을 갱신하고, 재발급 시 Access/Refresh Token을 모두 회전한다.
- 비밀번호는 BCrypt로 저장한다.
- 공개 GET API와 회원가입·로그인·재발급·Swagger 경로만 Security Filter Chain에서 허용한다.
- 역할 API는 Controller의 `@PreAuthorize("hasRole(...)")`로 제한한다.
- Service는 본인 예약, 담당 의사 예약, 문진 소유권을 다시 검증한다.
- 프론트엔드는 라우트 가드로 화면 접근을 나누지만 최종 권한 판단은 백엔드가 담당한다.
- 프론트 토큰은 `localStorage`에 저장되고, 401 응답 시 재발급을 한 번 공유 실행한 뒤 원 요청을 재시도한다.

## 10. 예외 처리

- 의도한 업무 오류는 `BusinessException`과 `ErrorCode`로 표현한다.
- `GlobalExceptionHandler`가 업무 예외, 권한 부족, Bean Validation, 잘못된 JSON/파라미터를 공통 실패 응답으로 변환한다.
- 미인증 요청은 `CustomAuthenticationEntryPoint`가 `AUTH_007`과 HTTP 401을 반환한다.
- 메서드 권한 부족은 `AUTH_006`과 HTTP 403을 반환한다.
- 처리하지 못한 예외는 구체 정보를 노출하지 않고 `INTERNAL_SERVER_ERROR`로 반환한다.
- 새 오류가 필요하면 기존 `ErrorCode` 재사용 가능성을 먼저 검토하고 메시지는 코드에 중앙화한다.

## 11. 데이터베이스 설계 원칙

- PK는 MySQL IDENTITY 기반 `Long`을 사용한다.
- `User`와 역할별 프로필을 분리하고 `Patient.user_id`, `Doctor.user_id`에 UNIQUE를 둔다.
- 예약은 `Patient`와 `DoctorSchedule`을 연결한다. 슬롯의 예약 가능 여부는 `DoctorSchedule.status`가 관리한다.
- 예약과 문진, 문진과 분석은 각각 UNIQUE FK로 0..1 관계를 강제한다.
- `BaseEntity` 상속 Entity는 생성·수정·삭제 시각을 가진다. `DoctorSchedule`은 현재 `BaseEntity`를 상속하지 않는다.
- Soft delete는 `deleted_at`과 상태 변경으로 구현되어 있으나 전역 Hibernate 필터는 없다. 각 조회가 상태 또는 활성 사용자 조건을 명시해야 한다.
- 개발 프로파일의 기본 DDL 전략은 `update`, 운영 프로파일은 `validate`다. 저장소에는 Flyway/Liquibase나 별도 스키마 마이그레이션 파일이 없다.

## 12. 테스트 전략

현재 백엔드에는 JUnit 5 기반 테스트가 있으며 다음 층을 조합한다.

- Mockito Service 단위 테스트: 업무 규칙과 Repository 상호작용
- `@WebMvcTest`/MockMvc 테스트: 요청 검증, 공통 응답, 공개·역할별 보안 경계
- `@DataJpaTest`: Repository 메서드, UNIQUE 제약, QueryDSL 검색과 페이징
- Entity 단위 테스트: 상태 전이와 시간 검증
- Gemini 변환 테스트 및 Fake analyzer 테스트: 외부 호출 없이 분석 계약 검증
- `@SpringBootTest`: 애플리케이션 Context 로드

CI는 MySQL 8.4 서비스와 test 프로파일로 `./gradlew test`, 백엔드 빌드, 프론트 `npm run lint`와 `npm run build`를 실행한다. 프론트엔드에는 현재 자동화된 테스트 스크립트나 테스트 파일이 없다.

## 13. 코드 작성 시 주요 규칙

1. 작업 전 Entity → Repository → Service → Controller → DTO → Security 순으로 영향 범위를 확인한다.
2. 인증된 사용자의 ID를 클라이언트 입력으로 신뢰하지 않는다.
3. 본인 데이터나 담당 데이터인지 Service에서 검증한다.
4. Request와 Response 계약을 바꿀 때 백엔드 DTO, 프론트 타입, API 함수, 화면을 함께 확인한다.
5. 새 상태 전이는 Entity 메서드와 테스트로 보호한다.
6. 동시 접근 가능성이 있는 예약·토큰 로직은 애플리케이션 검사만으로 충분한지 DB 제약과 Lock까지 검토한다.
7. 외부 AI 오류가 문진 원본 저장을 훼손하지 않도록 트랜잭션 경계를 유지한다.
8. 운영 설정에 비밀값이나 고정 호스트를 커밋하지 않고 환경변수를 사용한다.
9. 새 의존성은 현재 문제를 해결하는 데 반드시 필요할 때만 추가한다.
10. 상세 계약은 [API.md](API.md), 데이터 관계는 [ERD.md](ERD.md), 개선 과제는 [ROADMAP.md](ROADMAP.md)를 함께 갱신한다.
