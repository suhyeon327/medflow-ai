# MedFlow API 명세

> 기준일: 2026-08-20  
> Swagger 설명이 아니라 현재 Controller, DTO, Service, Security 설정을 기준으로 검증했다.

## 1. 공통 규칙

- Base URL: `/api/v1`
- 인증 헤더: `Authorization: Bearer <accessToken>`
- 날짜: `yyyy-MM-dd`
- 시간: `HH:mm:ss`
- 일시: ISO-8601 LocalDateTime(예: `2026-08-20T09:30:00`)
- 역할: `PATIENT`, `DOCTOR`, `ADMIN`
- Swagger UI: `/swagger-ui.html`
- OpenAPI JSON: `/v3/api-docs`

### 성공 응답

- 조회 및 응답 본문이 있는 수정/삭제: `200 OK`
- 자원 생성: `201 Created`
- 응답 본문이 없는 성공: `204 No Content`

```json
{
  "success": true,
  "data": {},
  "timestamp": "2026-08-20T12:00:00"
}
```

### 실패 응답

```json
{
  "success": false,
  "error": {
    "code": "RESERVATION_005",
    "message": "예약을 찾을 수 없습니다."
  },
  "timestamp": "2026-08-20T12:00:00"
}
```

### 공통 오류

| HTTP | Code | 조건 |
| --- | --- | --- |
| 400 | `VALIDATION_ERROR` | `@Valid` 실패, 잘못된 JSON/Enum, Query/Path 타입 불일치 |
| 401 | `AUTH_007` | 인증이 필요한 Endpoint에 유효한 Access Token이 없음 |
| 403 | `AUTH_006` | `@PreAuthorize` 역할 불일치 또는 명시적 소유권 예외 일부 |
| 500 | `INTERNAL_SERVER_ERROR` | 처리되지 않은 서버 오류 |

페이지 Query는 별도 표기가 없으면 `page`(0부터), `size`, `sort=field,direction`을 받을 수 있다. 실제 응답은 도메인별 Page DTO다.

## 2. Authentication

| Method | URL | 인증 / Role | Parameter | Request DTO | Response DTO (`data`) | 주요 Error |
| --- | --- | --- | --- | --- | --- | --- |
| POST | `/api/v1/auth/signup` | 불필요 | 없음 | `SignupRequest` | `SignupResponse` | `AUTH_001`, `AUTH_008`, `AUTH_009`, `PATIENT_002`, `HOSPITAL_002`, `DOCTOR_001` |
| POST | `/api/v1/auth/login` | 불필요 | 없음 | `LoginRequest` | `JwtToken` | `AUTH_005` |
| POST | `/api/v1/auth/reissue` | 불필요 | 없음 | `ReissueRequest` | `JwtToken` | `AUTH_005` |
| POST | `/api/v1/auth/logout` | 필요 / 모든 인증 사용자 | 없음 | `LogoutRequest` | `Void` (`null`) | 다른 사용자의 토큰이면 `AUTH_006`; 저장되지 않은 토큰이면 성공 |
| DELETE | `/api/v1/auth/withdraw` | 필요 / 모든 인증 사용자 | 없음 | `WithdrawRequest` | `WithdrawResponse` | `AUTH_002`, `AUTH_003` |

### Request

`SignupRequest`

```json
{
  "email": "patient@example.com",
  "password": "password123",
  "role": "PATIENT",
  "patient": {
    "name": "홍길동",
    "birth": "1990-01-01",
    "gender": "MALE",
    "phone": "010-1234-5678"
  },
  "doctor": null
}
```

- `email`: 필수, 이메일, 최대 100자
- `password`: 필수, 8~20자
- `role`: `PATIENT` 또는 `DOCTOR`만 허용
- `PATIENT`: `patient`만 필수. `name` 최대 50자, `birth`, `gender`, 전화번호 형식 필수
- `DOCTOR`: `doctor`만 필수. `hospitalId`, `name` 최대 50자, `licenseNumber` 최대 30자, `specialty` 최대 100자 필수; `introduction` 최대 1000자, `contact` 최대 20자 선택

| DTO | 필드 |
| --- | --- |
| `LoginRequest` | `email`, `password` |
| `ReissueRequest` | `refreshToken` |
| `LogoutRequest` | `refreshToken` |
| `WithdrawRequest` | `password` |
| `SignupResponse` | `id`, `email`, `role`, `profileId`, `profileStatus`(의사만 값 존재) |
| `JwtToken` | `grantType`=`Bearer`, `accessToken`, `refreshToken` |
| `WithdrawResponse` | `id`, `deleteAt`, `message` |

## 3. Public Hospital / Doctor

| Method | URL | 인증 / Role | Path Parameter | Query Parameter | Request DTO | Response DTO (`data`) | 주요 Error |
| --- | --- | --- | --- | --- | --- | --- | --- |
| GET | `/api/v1/hospitals` | 불필요 | 없음 | `keyword?`, `page?`, `size?`, `sort?`; 기본 size 20 | 없음 | `HospitalPageResponse` | 타입/정렬 오류 시 공통 오류 |
| GET | `/api/v1/hospitals/{hospitalId}` | 불필요 | `hospitalId` | 없음 | 없음 | `HospitalDetailResponse` | `HOSPITAL_002` |
| GET | `/api/v1/hospitals/{hospitalId}/doctors` | 불필요 | `hospitalId` | 없음 | 없음 | `List<DoctorResponse>` | `HOSPITAL_002` |
| GET | `/api/v1/doctors/{doctorId}` | 불필요 | 숫자 `doctorId` | 없음 | 없음 | `DoctorDetailResponse` | `DOCTOR_002` |
| GET | `/api/v1/doctors/{doctorId}/available-schedules` | 불필요 | `doctorId` | `date?` | 없음 | `List<AvailableDoctorScheduleResponse>` | `DOCTOR_002` |

공개 병원은 `ACTIVE`만, 공개 의사는 `DoctorStatus.ACTIVE`이면서 연결된 User도 `UserStatus.ACTIVE`인 경우만 반환한다.

| Response DTO | 필드 |
| --- | --- |
| `HospitalPageResponse` | `content`, `page`, `size`, `totalElements`, `totalPages`, `first`, `last` |
| `HospitalListResponse` | `id`, `name`, `region`, `address`, `tel`, `doctorCount`, `specialties` |
| `HospitalDetailResponse` | `id`, `name`, `address`, `region`, `tel` |
| `DoctorResponse` | `doctorId`, `doctorName`, `hospitalId`, `hospitalName`, `specialty`, `introduction`, `contact` |
| `DoctorDetailResponse` | `doctorId`, `doctorName`, `hospitalId`, `hospitalName`, `specialty`, `introduction`, `contact` |
| `AvailableDoctorScheduleResponse` | `scheduleId`, `date`, `startTime`, `endTime` |

## 4. Patient Profile

| Method | URL | 인증 / Role | Parameter | Request DTO | Response DTO (`data`) | 주요 Error |
| --- | --- | --- | --- | --- | --- | --- |
| GET | `/api/v1/patients/profile` | 필요 / `PATIENT` | 없음 | 없음 | `PatientResponse` | `PATIENT_001` |
| PUT | `/api/v1/patients/profile` | 필요 / `PATIENT` | 없음 | `PatientRequest` | `PatientResponse` | `PATIENT_001` |

| DTO | 필드 / 검증 |
| --- | --- |
| `PatientRequest` | `name` 필수, `birth` 필수, `gender` 필수, `phone` 하이픈 없는 10~11자리 |
| `PatientResponse` | `id`, `name`, `birth`, `gender`, `phone` |

## 5. Patient Reservation

| Method | URL | 인증 / Role | Path Parameter | Query Parameter | Request DTO | Response DTO (`data`) | 주요 Error |
| --- | --- | --- | --- | --- | --- | --- | --- |
| POST | `/api/v1/reservations` | 필요 / `PATIENT` | 없음 | 없음 | `ReservationCreateRequest` | `ReservationCreateResponse` | `RESERVATION_004`, `RESERVATION_001`, `PATIENT_001` |
| GET | `/api/v1/reservations/patient` | 필요 / `PATIENT` | 없음 | `status?`, `date?`, `hospitalId?`, `doctorId?`, `period?`, `page?`, `size?`; 기본 size 10 | 없음 | `PatientReservationPageResponse` | `PATIENT_001` |
| PATCH | `/api/v1/reservations/{reservationId}/cancel` | 필요 / `PATIENT` | `reservationId` | 없음 | 없음 | `ReservationCancelResponse` | `PATIENT_001`, `RESERVATION_005`, `RESERVATION_002`, `RESERVATION_007` |

- `ReservationCreateRequest`: `scheduleId` 필수
- `status`: 실제 백엔드 Enum `APPROVED`, `COMPLETED`, `CANCELLED`
- `period`: `UPCOMING`, `TODAY`, `PAST`
- 기간 정렬: `UPCOMING` 오름차순, `PAST` 내림차순, 그 외 날짜 내림차순/시간 오름차순

| Response DTO | 필드 |
| --- | --- |
| `ReservationCreateResponse` | `reservationId`, `status` |
| `PatientReservationPageResponse` | `content`, `page`, `size`, `totalElements`, `totalPages` |
| `PatientReservationResponse` | `reservationId`, `hospitalId`, `hospitalName`, `doctorId`, `doctorName`, `reservationDate`, `startTime`, `endTime`, `reservationStatus`, `questionnaireId` |
| `ReservationCancelResponse` | `reservationId`, `status` |

## 6. Patient Questionnaire

| Method | URL | 인증 / Role | Path Parameter | Query Parameter | Request DTO | Response DTO (`data`) | 주요 Error |
| --- | --- | --- | --- | --- | --- | --- | --- |
| POST | `/api/v1/questionnaires` | 필요 / `PATIENT` | 없음 | 없음 | `QuestionnaireCreateRequest` | `QuestionnaireResponse` | `PATIENT_001`, `RESERVATION_005`, `QUESTIONNAIRE_001`~`004` |
| GET | `/api/v1/questionnaires/{questionnaireId}` | 필요 / `PATIENT` | `questionnaireId` | 없음 | 없음 | `QuestionnaireDetailResponse` | `PATIENT_001`, `QUESTIONNAIRE_002`, `QUESTIONNAIRE_005` |
| PUT | `/api/v1/questionnaires/{questionnaireId}` | 필요 / `PATIENT` | `questionnaireId` | 없음 | `QuestionnaireUpdateRequest` | `QuestionnaireUpdateResponse` | `PATIENT_001`, `QUESTIONNAIRE_002`~`007` |
| GET | `/api/v1/questionnaires/{questionnaireId}/analysis` | 필요 / `PATIENT` | `questionnaireId` | 없음 | 없음 | `QuestionnaireAnalysisDetailResponse` | `PATIENT_001`, `QUESTIONNAIRE_002`, `QUESTIONNAIRE_005`, `QUESTIONNAIRE_007` |

### Request DTO

`QuestionnaireCreateRequest`는 아래 필드 전체와 `reservationId`를 받는다. `QuestionnaireUpdateRequest`는 `reservationId`를 제외하고 동일하다.

| 필드 | 필수 | 검증 / 형식 |
| --- | --- | --- |
| `chiefComplaint` | 예 | 빈 문자열 불가 |
| `symptomStartedAt` | 예 | LocalDateTime |
| `symptomDescription` | 예 | 빈 문자열 불가 |
| `painLevel` | 아니오 | 0~10 |
| `temperature` | 아니오 | BigDecimal; DTO의 서버 범위 검증은 없음 |
| `associatedSymptoms` | 아니오 | 문자열 |
| `medicalHistory` | 아니오 | 문자열 |
| `medications` | 아니오 | 문자열 |
| `allergies` | 아니오 | 문자열 |
| `additionalNote` | 아니오 | 문자열 |

### Response DTO

| DTO | 필드 |
| --- | --- |
| `QuestionnaireResponse` | `questionnaireId`, `reservationId`, `patientId`, 문진 필드, `createdAt` |
| `QuestionnaireDetailResponse` | 위 필드 + `updatedAt` |
| `QuestionnaireUpdateResponse` | `questionnaireId`, `reservationId`, `patientId`, 문진 필드, `updatedAt` |
| `QuestionnaireAnalysisDetailResponse` | `analysisId`, `questionnaireId`, `summary`, `keyFindings`, `riskSignals`, `doctorCheckpoints`, `priorityLevel`, `status` |

분석 상태는 `PENDING`, `PROCESSING`, `COMPLETED`, `FAILED`다. 우선순위는 `NORMAL`, `CAUTION`, `HIGH_PRIORITY`이며 응급도 판정이 아니다. Gemini 오류는 문진 요청 자체를 실패시키지 않고 분석을 `FAILED`로 저장하도록 처리된다.

## 7. Doctor Profile / Schedule

| Method | URL | 인증 / Role | Path Parameter | Query Parameter | Request DTO | Response DTO (`data`) | 주요 Error |
| --- | --- | --- | --- | --- | --- | --- | --- |
| GET | `/api/v1/doctors/profile` | 필요 / `DOCTOR` | 없음 | 없음 | 없음 | `DoctorProfileResponse` | `DOCTOR_002` |
| PUT | `/api/v1/doctors/profile` | 필요 / `DOCTOR` | 없음 | 없음 | `DoctorUpdateRequest` | `DoctorProfileResponse` | `DOCTOR_002`, `HOSPITAL_002`, `DOCTOR_003`, `DOCTOR_004` |
| POST | `/api/v1/doctors/schedules` | 필요 / `DOCTOR` | 없음 | 없음 | `DoctorScheduleCreateRequest` | `List<DoctorScheduleResponse>` | `DOCTOR_002`, `DOCTOR_005`, `DOCTOR_006`, `RESERVATION_001` |
| GET | `/api/v1/doctors/schedules` | 필요 / `DOCTOR` | 없음 | `date?` | 없음 | `List<DoctorScheduleResponse>` | `DOCTOR_002` |

| Request DTO | 필드 / 검증 |
| --- | --- |
| `DoctorUpdateRequest` | `hospitalId`, `name`, `licenseNumber` 필수; `specialty` 최대 100, `introduction` 최대 1000, `contact` 최대 20 |
| `DoctorScheduleCreateRequest` | `date`, `startTime`, `endTime`, `slotMinutes` 필수; `slotMinutes` 10~60 |

| Response DTO | 필드 |
| --- | --- |
| `DoctorProfileResponse` | `doctorId`, `doctorName`, `licenseNumber`, `hospitalId`, `hospitalName`, `specialty`, `introduction`, `contact`, `status` |
| `DoctorScheduleResponse` | `scheduleId`, `date`, `startTime`, `endTime` |

POST는 `[startTime, endTime)` 구간을 `slotMinutes` 단위로 나눠 여러 슬롯을 반환한다. `endTime`을 넘어가는 마지막 불완전 구간은 생성하지 않는다.

## 8. Doctor Reservation / Questionnaire

| Method | URL | 인증 / Role | Path Parameter | Query Parameter | Request DTO | Response DTO (`data`) | 주요 Error |
| --- | --- | --- | --- | --- | --- | --- | --- |
| GET | `/api/v1/doctors/reservations` | 필요 / `DOCTOR` | 없음 | `date?`, `status?`, `page?`, `size?`, `sort?`; 기본 size 10 | 없음 | `DoctorReservationPageResponse` | `DOCTOR_002` |
| GET | `/api/v1/doctors/reservations/{reservationId}/patient` | 필요 / `DOCTOR` | `reservationId` | 없음 | 없음 | `DoctorReservationPatientResponse` | `DOCTOR_002`, `RESERVATION_005`, `PATIENT_001` |
| PATCH | `/api/v1/doctors/reservations/{reservationId}/status` | 필요 / `DOCTOR` | `reservationId` | 없음 | `ReservationStatusUpdateRequest` | `ReservationStatusResponse` | `DOCTOR_002`, `RESERVATION_003`, `RESERVATION_005` |
| GET | `/api/v1/doctors/questionnaires/{questionnaireId}/analysis` | 필요 / `DOCTOR` | `questionnaireId` | 없음 | 없음 | `DoctorQuestionnaireAnalysisResponse` | `DOCTOR_002`, `QUESTIONNAIRE_002`, `QUESTIONNAIRE_005`, `QUESTIONNAIRE_007` |

- `ReservationStatusUpdateRequest.status`는 `COMPLETED`만 허용한다.
- 완료 Endpoint는 담당 의사의 예약을 Pessimistic Write Lock으로 읽고 `APPROVED` 상태인 경우에만 처리한다. 예약 시간 경과 여부는 완료 조건으로 사용하지 않는다.

| Response DTO | 필드 |
| --- | --- |
| `DoctorReservationPageResponse` | `content`, `page`, `size`, `totalElements`, `totalPages` |
| `DoctorReservationResponse` | `reservationId`, `patientName`, `reservationDate`, `startTime`, `endTime`, `reservationStatus`, `questionnaireId` |
| `DoctorReservationPatientResponse` | `patientId`, `patientName`, `gender`, `birthDate`, `phoneNumber`, `reservationId`, 예약 일시와 상태 |
| `ReservationStatusResponse` | `reservationId`, `status` |
| `DoctorQuestionnaireAnalysisResponse` | `analysisId`, `questionnaireId`, `reservationId`, 분석 결과 필드, `priorityLevel`, `status` |

## 9. Admin Hospital

| Method | URL | 인증 / Role | Path Parameter | Query Parameter | Request DTO | Response DTO (`data`) | 주요 Error |
| --- | --- | --- | --- | --- | --- | --- | --- |
| POST | `/api/v1/admin/hospitals` | 필요 / `ADMIN` | 없음 | 없음 | `AdminHospitalCreateRequest` | `AdminHospitalResponse` | `HOSPITAL_001` |
| GET | `/api/v1/admin/hospitals` | 필요 / `ADMIN` | 없음 | 없음 | 없음 | `List<AdminHospitalResponse>` | 공통 오류 |
| PUT | `/api/v1/admin/hospitals/{hospitalId}` | 필요 / `ADMIN` | `hospitalId` | 없음 | `AdminHospitalUpdateRequest` | `AdminHospitalResponse` | `HOSPITAL_001`, `HOSPITAL_002` |
| DELETE | `/api/v1/admin/hospitals/{hospitalId}` | 필요 / `ADMIN` | `hospitalId` | 없음 | 없음 | `AdminHospitalDeleteResponse` | `HOSPITAL_002` |

| DTO | 필드 |
| --- | --- |
| `AdminHospitalCreateRequest` | `name`, `address`, `region`, `tel`: 모두 필수 |
| `AdminHospitalUpdateRequest` | 생성 필드 + `status`(`ACTIVE`, `CLOSED`) 필수 |
| `AdminHospitalResponse` | `id`, 병원 필드, `status`, `createdAt`, `updatedAt`, `deletedAt` |
| `AdminHospitalDeleteResponse` | `hospitalId`, `deleteAt`, `message` |

DELETE는 행을 물리 삭제하지 않고 `status=CLOSED`, `deletedAt`을 설정한다.

## 10. Admin Doctor

| Method | URL | 인증 / Role | Path Parameter | Query Parameter | Request DTO | Response DTO (`data`) | 주요 Error |
| --- | --- | --- | --- | --- | --- | --- | --- |
| GET | `/api/v1/admin/doctors` | 필요 / `ADMIN` | 없음 | `status?` (`PENDING`, `ACTIVE`, `REJECTED`) | 없음 | `List<AdminDoctorListResponse>` | 잘못된 Enum은 `VALIDATION_ERROR` |
| GET | `/api/v1/admin/doctors/{doctorId}` | 필요 / `ADMIN` | `doctorId` | 없음 | 없음 | `AdminDoctorDetailResponse` | `DOCTOR_002` |
| PATCH | `/api/v1/admin/doctors/{doctorId}/approve` | 필요 / `ADMIN` | `doctorId` | 없음 | 없음 | `AdminDoctorApproveResponse` | `DOCTOR_002`, `DOCTOR_003` |
| PATCH | `/api/v1/admin/doctors/{doctorId}/reject` | 필요 / `ADMIN` | `doctorId` | 없음 | 없음 | `AdminDoctorRejectResponse` | `DOCTOR_002`, `DOCTOR_003` |

| Response DTO | 필드 |
| --- | --- |
| `AdminDoctorListResponse` | `doctorId`, `doctorName`, `hospitalName`, `licenseNumber`, `status` |
| `AdminDoctorDetailResponse` | 목록 필드 + `hospitalId`, `email`, `specialty`, `introduction`, `contact` |
| `AdminDoctorApproveResponse` | `doctorId`, `message` |
| `AdminDoctorRejectResponse` | `doctorId`, `message` |

## 11. Admin User

| Method | URL | 인증 / Role | Path Parameter | Query Parameter | Request DTO | Response DTO (`data`) | 주요 Error |
| --- | --- | --- | --- | --- | --- | --- | --- |
| GET | `/api/v1/admin/users` | 필요 / `ADMIN` | 없음 | `role?`, `status?`, `page?`, `size?`, `sort?`; 기본 size 20, `createdAt,DESC` | 없음 | `AdminUserPageResponse` | 잘못된 Enum은 `VALIDATION_ERROR` |
| GET | `/api/v1/admin/users/{userId}` | 필요 / `ADMIN` | `userId` | 없음 | 없음 | `AdminUserResponse` | `AUTH_003` |

| Response DTO | 필드 |
| --- | --- |
| `AdminUserPageResponse` | `content`, `page`, `size`, `totalElements`, `totalPages` |
| `AdminUserResponse` | `userId`, `email`, `role`, `status`, `createdAt`, `updatedAt`, `deletedAt` |

## 12. Admin Reservation

| Method | URL | 인증 / Role | Path Parameter | Query Parameter | Request DTO | Response DTO (`data`) | 주요 Error |
| --- | --- | --- | --- | --- | --- | --- | --- |
| GET | `/api/v1/admin/reservations` | 필요 / `ADMIN` | 없음 | `hospitalId?`, `doctorId?`, `patientId?`, `date?`, `status?`, `page?`, `size?`, `sort?`; 기본 size 20 | 없음 | `AdminReservationPageResponse` | 잘못된 타입/Enum은 `VALIDATION_ERROR` |

| Response DTO | 필드 |
| --- | --- |
| `AdminReservationPageResponse` | `content`, `page`, `size`, `totalElements`, `totalPages` |
| `AdminReservationResponse` | `reservationId`, 병원 ID/명, 의사 ID/명, 환자 ID/명, `reservationDate`, `startTime`, `endTime`, `reservationStatus`, `createdAt` |

허용 정렬 필드는 QueryDSL 구현 기준 `reservationDate`, `startTime`, `reservationStatus`, `createdAt`이다. 정렬이 없으면 예약일 내림차순, 시작 시간 오름차순을 사용한다.

## 13. Error Code 목록

| 영역 | Code |
| --- | --- |
| Auth | `AUTH_001` 이메일 중복, `AUTH_002` 비밀번호 불일치, `AUTH_003` 사용자 없음, `AUTH_004` 토큰 권한 없음, `AUTH_005` 자격 증명 오류, `AUTH_006` 권한 없음, `AUTH_007` 인증 필요, `AUTH_008` 가입 역할 오류, `AUTH_009` 역할 프로필 오류 |
| Patient | `PATIENT_001` 환자 없음, `PATIENT_002` 환자 프로필 중복 |
| Hospital | `HOSPITAL_001` 병원 중복, `HOSPITAL_002` 병원 없음 |
| Doctor | `DOCTOR_001` 의사/면허 중복, `DOCTOR_002` 의사 없음, `DOCTOR_003` 상태 전이 불가, `DOCTOR_004` 면허 중복, `DOCTOR_005` 미승인 의사, `DOCTOR_006` 잘못된 시간 범위 |
| Reservation | `RESERVATION_001` 슬롯 이용 불가, `002` 완료 예약 취소 불가, `003` 상태 변경 불가, `004` 슬롯 없음, `005` 예약 없음, `006` 종료 전 완료 불가, `007` 이미 취소 |
| Questionnaire | `QUESTIONNAIRE_001` 중복, `002` 예약 접근 불가, `003` 취소 예약, `004` 완료 예약, `005` 문진 없음, `006` 시작 후 수정, `007` 분석 없음 |
| AI | `AI_001` 분석 실패, `AI_002` Gemini Key 없음 |

`AI_001`은 분석 Service 내부에서 잡혀 `QuestionnaireAnalysis.status=FAILED`로 저장되는 경로가 기본이므로 일반적인 문진 생성 API 응답에 직접 노출되지 않는다. `AI_002`는 Gemini provider로 애플리케이션을 시작할 때 Key가 없으면 발생할 수 있다.
