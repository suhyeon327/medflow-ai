# MedFlow 구현 요구사항

> 기준일: 2026-08-20  
> 상태는 백엔드 API와 현재 React 화면을 함께 확인한 결과다.

## 상태 정의

| 상태 | 의미 |
| --- | --- |
| `IMPLEMENTED` | 현재 코드에서 주요 사용자 흐름과 권한 검증이 연결되어 있다. |
| `PARTIAL` | 일부 계층만 구현되었거나, 현재 규모·업무 규칙에서 명확한 제약이 있다. |
| `NOT IMPLEMENTED` | 현재 Controller/API 또는 사용자 화면에서 해당 동작을 제공하지 않는다. |

## PUBLIC

| 요구사항 | 상태 | 현재 구현 근거 및 범위 |
| --- | --- | --- |
| 활성 병원 목록 조회 | `IMPLEMENTED` | 병원명·지역·주소 통합 검색, 페이징, 활성 병원만 공개한다. |
| 병원 상세 및 소속 의사 조회 | `IMPLEMENTED` | 활성 병원과 `ACTIVE` 의사 중 사용자 계정도 `ACTIVE`인 의사만 노출한다. |
| 의사 상세 조회 | `IMPLEMENTED` | 승인된 활성 의사만 공개한다. 연락처 미등록 시 병원 전화번호를 사용한다. |
| 예약 가능 슬롯 조회 | `IMPLEMENTED` | 의사별 `AVAILABLE` 슬롯을 날짜 조건과 함께 조회할 수 있다. 프론트는 지난 슬롯을 추가로 숨긴다. |
| 환자 회원가입 | `IMPLEMENTED` | 계정과 환자 프로필을 한 트랜잭션에서 생성하고 전화번호의 하이픈을 제거한다. |
| 의사 회원가입 | `IMPLEMENTED` | 병원·면허번호를 검증하고 `PENDING` 의사 프로필을 생성한다. 관리자 승인 전 공개/슬롯 생성은 제한된다. |
| 관리자 공개 회원가입 | `NOT IMPLEMENTED` | API가 `PATIENT`, `DOCTOR`만 허용한다. 관리자는 환경변수 기반 bootstrap으로만 생성된다. |
| 로그인 | `IMPLEMENTED` | 이메일/비밀번호 인증 후 60분 Access Token과 14일 Refresh Token을 발급한다. |
| 토큰 재발급 | `IMPLEMENTED` | DB에 저장된 Refresh Token과 JWT 만료를 검증하고 두 토큰을 회전한다. |
| 이메일 인증·비밀번호 찾기/재설정 | `NOT IMPLEMENTED` | 관련 API, 이메일 연동, 화면이 없다. |

## PATIENT

| 요구사항 | 상태 | 현재 구현 근거 및 범위 |
| --- | --- | --- |
| 환자 프로필 조회·수정 | `IMPLEMENTED` | 로그인 사용자 ID로 자신의 프로필만 조회·수정한다. |
| 환자 프로필 수정 입력 검증 | `PARTIAL` | `PatientRequest`에 검증 애너테이션은 있으나 Controller의 수정 Endpoint에 `@Valid`가 없어 서버 Bean Validation이 실행되지 않는다. 프론트는 이름·전화번호를 별도 검증한다. |
| 슬롯 예약 | `IMPLEMENTED` | `AVAILABLE` 슬롯을 선택하면 예약을 `APPROVED`, 슬롯을 `RESERVED`로 변경한다. |
| 동시 예약 방지 | `PARTIAL` | 상태 검사와 단일 트랜잭션은 있으나 예약 생성 시 슬롯 Lock 또는 `reservations.doctor_schedule_id` UNIQUE 제약이 없다. 경합 시 중복 예약 가능성을 DB 수준에서 차단하지 못한다. |
| 내 예약 검색·페이징 | `IMPLEMENTED` | 상태, 날짜, 병원 ID, 의사 ID, 기간(`UPCOMING`, `TODAY`, `PAST`)으로 조회한다. |
| 예약 취소 | `PARTIAL` | 자신의 예약만 취소하고 슬롯을 해제한다. 완료·이미 취소 상태는 거부하지만, 백엔드는 진료 시작 이후이면서 아직 `APPROVED`인 예약의 취소를 별도로 막지 않는다. |
| 예약 변경/재예약 | `NOT IMPLEMENTED` | 기존 예약의 슬롯을 바꾸는 API와 화면이 없다. 취소 후 새 예약만 가능하다. |
| 문진 작성 | `IMPLEMENTED` | 자신의 취소·완료되지 않은 예약에 예약당 하나의 문진을 생성한다. |
| 문진 조회·수정 | `IMPLEMENTED` | 자신의 문진만 조회하며 예약 시작 전까지만 수정한다. 수정 시 분석 결과를 `PENDING`으로 초기화한다. |
| 문진 AI 분석 조회 | `IMPLEMENTED` | 분석 상태와 요약·핵심 내용·주의 신호·의사 확인 항목·우선순위를 조회하고 처리 중에는 프론트가 3초 간격으로 갱신한다. |
| 분석 실패 재시도 요청 | `NOT IMPLEMENTED` | 실패 상태는 저장되지만 사용자가 다시 분석을 요청하는 Endpoint나 화면은 없다. 문진 수정 시에만 재분석이 발생한다. |
| 로그아웃 | `IMPLEMENTED` | 요청한 Refresh Token이 본인 소유일 때 삭제하고 프론트 인증 상태와 Query cache를 비운다. |
| 회원 탈퇴 | `IMPLEMENTED` | 비밀번호 확인 후 사용자와 환자 프로필을 soft delete하고 Refresh Token을 삭제한다. |

## DOCTOR

| 요구사항 | 상태 | 현재 구현 근거 및 범위 |
| --- | --- | --- |
| 의사 프로필 조회·수정 | `IMPLEMENTED` | 자신의 의사 프로필을 조회한다. 승인 후에는 인증 정보(병원·이름·면허번호)를 바꿀 수 없고 진료과·소개·연락처만 갱신할 수 있다. |
| 승인 상태 확인 | `IMPLEMENTED` | 프로필 화면에 `PENDING`, `ACTIVE`, `REJECTED`를 표시한다. |
| 진료 슬롯 일괄 생성 | `IMPLEMENTED` | 시작·종료 시각과 10~60분 간격으로 여러 슬롯을 만들며 시간 중첩과 동일 시작 슬롯을 차단한다. 승인된 의사만 가능하다. |
| 슬롯 조회 | `IMPLEMENTED` | 자신의 전체 슬롯 또는 특정 날짜 슬롯을 조회한다. |
| 슬롯 수정·삭제·휴진 처리 | `NOT IMPLEMENTED` | 생성된 슬롯을 변경하거나 제거하는 Endpoint와 화면이 없다. |
| 담당 예약 조회·검색 | `IMPLEMENTED` | 자신의 슬롯에 연결된 예약만 날짜·상태·페이징 조건으로 조회하고 문진 ID를 함께 반환한다. |
| 담당 예약 환자 정보 조회 | `IMPLEMENTED` | 담당 예약인지 검증한 후 환자 기본 정보와 예약 시간을 반환한다. |
| 진료 완료 처리 | `IMPLEMENTED` | 담당 의사만 자신의 `APPROVED` 예약을 `COMPLETED`로 변경할 수 있다. 의사 예약 화면은 `APPROVED` 예약에만 진료 완료 버튼을 제공하며, 예약 시간 경과만으로 자동 완료하지 않는다. |
| 담당 문진 AI 분석 조회 | `IMPLEMENTED` | 문진이 자신의 담당 예약에 연결되어 있는지 검증한 뒤 분석 결과를 반환한다. |
| 문진 원문 전체 조회 | `PARTIAL` | 의사용 응답은 AI 분석 결과를 제공하지만 환자가 입력한 전체 원문 필드를 직접 반환하는 전용 의사 Endpoint는 없다. |
| 의사 회원 탈퇴 화면 | `PARTIAL` | 공통 탈퇴 API는 모든 인증 사용자에게 열려 있으나 프론트 탈퇴 경로는 `PATIENT` 역할 아래에만 배치되어 있다. 의사 프로필의 `deleted_at`도 탈퇴 시 갱신하지 않는다. |

## ADMIN

| 요구사항 | 상태 | 현재 구현 근거 및 범위 |
| --- | --- | --- |
| 관리자 계정 초기 생성 | `IMPLEMENTED` | `ADMIN_BOOTSTRAP_ENABLED=true`일 때 환경변수 이메일·비밀번호로 계정을 한 번 생성한다. 공개 생성 API는 없다. |
| 관리자 대시보드 | `IMPLEMENTED` | 병원 수, 사용자 수, 예약 수·상태, 최근 사용자·예약을 기존 조회 API로 집계해 표시한다. |
| 병원 등록 | `IMPLEMENTED` | 병원명 중복을 검사하고 `ACTIVE` 병원을 생성한다. |
| 병원 목록·수정·종료 | `IMPLEMENTED` | 전체 병원을 조회하고 정보·상태를 수정하며 삭제 요청은 `CLOSED`와 `deleted_at`으로 soft delete한다. |
| 의사 목록·상세·상태 필터 | `IMPLEMENTED` | 전체 또는 승인 상태별 의사와 인증 정보를 조회한다. |
| 의사 승인·반려 | `IMPLEMENTED` | `PENDING` 상태에서만 `ACTIVE` 또는 `REJECTED`로 전이한다. |
| 사용자 목록·상세 조회 | `IMPLEMENTED` | 역할·상태 필터와 페이징을 지원하고 단일 사용자를 조회한다. |
| 사용자 잠금·복구·강제 탈퇴 | `NOT IMPLEMENTED` | `UserStatus.LOCKED` Enum은 있지만 상태를 변경하는 관리자 API와 화면은 없다. |
| 전체 예약 조회 | `IMPLEMENTED` | 병원 ID, 의사 ID, 환자 ID, 날짜, 상태, 페이징 조건을 지원한다. |
| 관리자 화면의 예약 이름 검색 | `PARTIAL` | 화면은 서버에서 최대 100건만 받은 뒤 병원·의사·환자 이름을 클라이언트에서 필터링한다. 전체 데이터에 대한 검색·페이징 결과가 아니다. |
| 예약 상태 강제 변경·취소 | `NOT IMPLEMENTED` | 관리자는 예약을 조회만 하며 상태를 변경하는 Endpoint가 없다. |

## 공통 비기능 요구사항의 현재 상태

| 요구사항 | 상태 | 현재 구현 |
| --- | --- | --- |
| 역할 기반 API 인가 | `IMPLEMENTED` | Spring Security와 Controller `@PreAuthorize`를 사용한다. |
| 소유권·담당 관계 검증 | `IMPLEMENTED` | 환자 예약·문진과 의사 담당 예약·문진을 Service에서 검증한다. |
| 공통 API 응답·오류 코드 | `IMPLEMENTED` | `ApiResponse`, `ErrorResponse`, `GlobalExceptionHandler`, `ErrorCode`를 사용한다. |
| 운영 DB 스키마 이력 관리 | `NOT IMPLEMENTED` | 운영은 Hibernate `validate`지만 Flyway/Liquibase/DDL 마이그레이션 파일이 없다. |
| 백엔드 자동 테스트 | `IMPLEMENTED` | Service, MVC Security, Repository, Entity, AI 분석 테스트가 있다. |
| 프론트엔드 자동 테스트 | `NOT IMPLEMENTED` | lint/build는 있으나 테스트 도구, 스크립트, 테스트 파일이 없다. |
| CI 및 자동 배포 | `IMPLEMENTED` | `main` 기준 GitHub Actions가 백엔드 테스트·빌드와 프론트 lint·build 후 EC2에 SSH로 Docker Compose 배포한다. |
