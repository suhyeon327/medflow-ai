# MedFlow AI API

> 기준일: 2026-08-04  
> 근거: Controller, DTO, Service, SecurityConfig, ErrorCode, GlobalExceptionHandler  
> Base URL/Host: [작성자 확인 필요: 배포 환경별 API Host]

## 1. 공통 규칙

### 인증 헤더

공개 API를 제외한 요청은 다음 헤더의 유효한 Access Token이 필요하다.

```http
Authorization: Bearer {accessToken}
```

### 공통 성공 응답

모든 Controller는 `ApiResponse<T>`를 반환한다.

```json
{
  "success": true,
  "data": {},
  "timestamp": "2026-08-04T12:00:00"
}
```

### 공통 실패 응답

```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "오류 메시지"
  },
  "timestamp": "2026-08-04T12:00:00"
}
```

`@JsonInclude(NON_NULL)`이 적용되어 null인 `data` 또는 `error`는 JSON에서 제외된다.

### 공통 HTTP 상태

| 상황 | 상태 | 코드 |
|---|---:|---|
| Controller 정상 반환 | 200 | 별도 성공 코드 없음 |
| JWT 없음/유효하지 않음 | 401 | `AUTH_007` |
| `@PreAuthorize` 권한 부족 | 403 | `AUTH_006` |
| `@Valid` body 검증 실패 | 400 | `VALIDATION_ERROR` |
| Request parameter 타입 변환 실패 | 400 | `VALIDATION_ERROR` |
| BusinessException | ErrorCode별 | ErrorCode별 |
| 처리되지 않은 예외 | 500 | `INTERNAL_SERVER_ERROR` |

생성/삭제 API도 별도 `ResponseEntity` 또는 `@ResponseStatus`가 없어 현재 성공 상태는 모두 200이다.

## 2. 인증 API

Controller: `com.medflow.auth.controller.AuthController`

현재 Controller에 선언된 API는 전체 44개다.

| Method | Path | 요청 DTO | 응답 DTO | 인증 | Role |
|---|---|---|---|---|---|
| POST | `/api/v1/auth/signup` | `SignupRequest` | `SignupResponse` | 불필요 | 공개 |
| POST | `/api/v1/auth/login` | `LoginRequest` | `JwtToken` | 불필요 | 공개 |
| POST | `/api/v1/auth/reissue` | `ReissueRequest` | `JwtToken` | 필요 | 모든 인증 Role |
| POST | `/api/v1/auth/logout` | `LogoutRequest` | `Void` | 필요 | 모든 인증 Role |
| DELETE | `/api/v1/auth/withdraw` | 없음 | `WithdrawResponse` | 필요 | 모든 인증 Role |

DTO 필드:

- `SignupRequest`: `email`, `password`, `role`
- `SignupResponse`: `id`, `email`, `role`
- `LoginRequest`: `email`, `password`
- `JwtToken`: `grantType`, `accessToken`, `refreshToken`
- `ReissueRequest`/`LogoutRequest`: `refreshToken`
- `WithdrawResponse`: `id`, `deleteAt`, `message`

주요 예외:

- 409 `AUTH_001`: 이메일 중복
- 409 `AUTH_008`: ADMIN 역할 회원가입 요청
- 401 `AUTH_005`: 로그인 실패 또는 Refresh Token 무효/만료
- 404 `AUTH_003`: 사용자 없음
- 401 `AUTH_004`: JWT에 권한 claim 없음

`/reissue`는 기능상 Refresh Token을 처리하지만 공개 matcher에 없으므로 현재 Access Token도 필요하다. `WithdrawRequest` 클래스는 존재하지만 탈퇴 Controller에서 사용하지 않는다.

## 3. 사용자 관리 API

Controller: `com.medflow.user.controller.UserController`

| Method | Path | 요청 | 응답 DTO | 인증 | Role |
|---|---|---|---|---|---|
| GET | `/api/v1/user/{id}` | Path `id: Long` | `AdminUserResponse` | 필요 | ADMIN |
| GET | `/api/v1/user/` | 없음 | `List<AdminUserResponse>` | 필요 | ADMIN |

`AdminUserResponse`: `userId`, `email`, `role`, `status`, `createdAt`, `updatedAt`, `deletedAt`.

주요 예외: 404 `AUTH_003`(사용자 없음), 403 `AUTH_006`(권한 없음).

## 4. 환자 API

Controller: `com.medflow.patient.controller.PatientController`

| Method | Path | 요청 DTO | 응답 DTO | 인증 | Role |
|---|---|---|---|---|---|
| POST | `/api/v1/patient/create` | `PatientRequest` | `PatientResponse` | 필요 | 모든 인증 Role |
| GET | `/api/v1/patient/profile` | 없음 | `PatientResponse` | 필요 | 모든 인증 Role |
| PUT | `/api/v1/patient/update` | `PatientRequest` | `PatientResponse` | 필요 | 모든 인증 Role |
| GET | `/api/v1/patient/` | 없음 | `List<AdminPatientResponse>` | 필요 | ADMIN |

DTO 필드:

- `PatientRequest`: `name`, `birth`, `gender`, `phone`
- `PatientResponse`: `id`, `name`, `birth`, `gender`, `phone`
- `AdminPatientResponse`: `patientId`, `name`, `email`, `birth`, `gender`, `phone`, `status`, `createdAt`, `updatedAt`, `deletedAt`

`PatientRequest`에는 validation annotation이 있지만 Controller 파라미터에 `@Valid`가 없어 현재 요청 검증이 실행되지 않는다.

주요 예외:

- 404 `AUTH_003`: 연결 User 없음
- 404 `PATIENT_001`: Patient 프로필 없음
- 409 `PATIENT_002`: 이미 Patient 프로필 존재
- 403 `AUTH_006`: 관리자 목록 권한 없음

## 5. 병원 API

### 일반 병원 조회

Controller: `com.medflow.hospital.controller.HospitalController`

| Method | Path | 요청 | 응답 DTO | 인증 | Role |
|---|---|---|---|---|---|
| GET | `/api/v1/hospitals/` | 없음 | `List<HospitalListResponse>` | 필요 | 모든 인증 Role |
| GET | `/api/v1/hospitals/{hospitalId}` | Path `hospitalId: Long` | `HospitalDetailResponse` | 필요 | 모든 인증 Role |

### 관리자 병원 관리

Controller: `com.medflow.hospital.controller.AdminHospitalController`

| Method | Path | 요청 DTO | 응답 DTO | 인증 | Role |
|---|---|---|---|---|---|
| POST | `/api/v1/admin/hospitals/` | `HospitalCreateRequest` | `HospitalDetailResponse` | 필요 | ADMIN |
| GET | `/api/v1/admin/hospitals/` | 없음 | `List<AdminHospitalResponse>` | 필요 | ADMIN |
| PUT | `/api/v1/admin/hospitals/{hospitalId}` | `HospitalUpdateRequest` | `AdminHospitalResponse` | 필요 | ADMIN |
| DELETE | `/api/v1/admin/hospitals/{hospitalId}` | 없음 | `deleteResponse` | 필요 | ADMIN |

DTO 필드:

- `HospitalCreateRequest`: `name`, `address`, `region`, `tel`
- `HospitalUpdateRequest`: 위 필드 + `status`
- `HospitalListResponse`: `id`, `name`
- `HospitalDetailResponse`: `id`, `name`, `address`, `region`, `tel`
- `AdminHospitalResponse`: 상세 정보 + `status`, `createdAt`, `updatedAt`, `deletedAt`
- `deleteResponse`: `hospitalId`, `deleteAt`, `message`

주요 예외:

- 409 `HOSPITAL_001`: 병원명 중복
- 409 `HOSPITAL_002`: 병원 없음 (`ErrorCode`에 실제로 CONFLICT로 정의됨)
- 403 `AUTH_006`: 관리자 권한 없음

## 6. 의사 API

### 의사 신청/프로필

Controller: `com.medflow.doctor.controller.DoctorController`

| Method | Path | 요청 DTO | 응답 DTO | 인증 | Role |
|---|---|---|---|---|---|
| POST | `/doctor/apply` | `DoctorApplyRequest` | `DoctorApplyResponse` | 필요 | DOCTOR |
| GET | `/doctor/profile` | 없음 | `DoctorInfoResponse` | 필요 | 모든 인증 Role |
| PATCH | `/doctor/profile` | `DoctorUpdateRequest` | `DoctorUpdateResponse` | 필요 | DOCTOR, ADMIN |
| DELETE | `/doctor/profile` | 없음 | `DoctorDeleteResponse` | 필요 | DOCTOR |

DTO 필드:

- `DoctorApplyRequest`/`DoctorUpdateRequest`: `hospitalId`, `name`, `licenseNumber`
- `DoctorApplyResponse`: `doctorId`, `doctorName`, `doctorstatus`
- `DoctorInfoResponse`: `doctorId`, `doctorName`, `hospitalName`, `licenseNumber`, `status`
- `DoctorUpdateResponse`: `doctorId`, `doctorName`, `doctorstatus`
- `DoctorDeleteResponse`: `doctorId`, `message`

### 관리자 의사 관리

Controller: `com.medflow.doctor.controller.DoctorAdminController`

| Method | Path | 요청 | 응답 DTO | 인증 | Role |
|---|---|---|---|---|---|
| GET | `/admin/doctors` | Query `status?: DoctorStatus` | `List<DoctorListResponse>` | 필요 | ADMIN |
| GET | `/admin/doctors/{doctorId}` | Path `doctorId: Long` | `DoctorDetailResponse` | 필요 | ADMIN |
| PATCH | `/admin/doctors/{doctorId}/approve` | 없음 | `DoctorApproveResponse` | 필요 | ADMIN |
| PATCH | `/admin/doctors/{doctorId}/reject` | 없음 | `DoctorRejectResponse` | 필요 | ADMIN |

주요 응답 필드:

- `DoctorListResponse`: `doctorId`, `doctorName`, `hospitalName`, `licenseNumber`, `status`
- `DoctorDetailResponse`: 위 필드 + `hospitalId`, `email`
- 승인/반려 응답: `doctorId`, `message`

주요 예외:

- 409 `DOCTOR_001`: User 또는 면허번호 중복 신청
- 404 `DOCTOR_002`: Doctor 없음
- 400 `DOCTOR_003`: PENDING이 아닌 상태에서 수정/취소/승인/반려
- 409 `DOCTOR_004`: 수정할 면허번호 중복
- 403 `DOCTOR_005`: 승인되지 않은 Doctor의 스케줄 생성
- 409 `HOSPITAL_002`: Hospital 없음

## 7. 의사 스케줄 API

Controller: `com.medflow.doctor.controller.DoctorScheduleController`

| Method | Path | 요청 DTO | 응답 DTO | 인증 | Role |
|---|---|---|---|---|---|
| POST | `/api/v1/doctors/schedules` | `DoctorScheduleCreateRequest` | `List<DoctorScheduleResponse>` | 필요 | DOCTOR, ADMIN |
| GET | `/api/v1/doctors/{doctorId}/schedules` | Path `doctorId: Long` | `List<DoctorScheduleResponse>` | 필요 | 모든 인증 Role |

- `DoctorScheduleCreateRequest`: `date`, `startTime`, `endTime`, `slotMinutes`(10~60)
- `DoctorScheduleResponse`: `scheduleId`, `date`, `startTime`, `endTime`

생성 API는 로그인 userId로 Doctor를 찾으므로 Doctor 프로필이 없는 ADMIN 호출은 404 `DOCTOR_002`가 된다.

주요 예외: 404 `DOCTOR_002`, 403 `DOCTOR_005`, 400 `VALIDATION_ERROR`.

## 8. 환자 예약 API

Controller: `com.medflow.reservation.controller.ReservationController`  
클래스 권한: PATIENT

| Method | Path | 요청 | 응답 DTO | 인증 | Role |
|---|---|---|---|---|---|
| POST | `/api/v1/reservations/` | `ReservationCreateRequest` | `ReservationCreateResponse` | 필요 | PATIENT |
| GET | `/api/v1/reservations/patient` | Query filters + Pageable | `PatientReservationPageResponse` | 필요 | PATIENT |
| PATCH | `/api/v1/reservations/{reservationId}/cancel` | Path `reservationId` | `ReservationCancelResponse` | 필요 | PATIENT |

예약 목록 Query:

- `status?: ReservationStatus`
- `date?: LocalDate` (`yyyy-MM-dd`)
- `hospitalId?: Long`
- `doctorId?: Long`
- `period?: ReservationPeriod`
- Spring Pageable 파라미터(`page`, `size`, `sort`), 기본 size 10

DTO:

- `ReservationCreateRequest`: `scheduleId`
- `ReservationCreateResponse`: `reservationId`, `status`
- `PatientReservationResponse`: `reservationId`, `hospitalId`, `hospitalName`, `doctorId`, `doctorName`, `reservationDate`, `startTime`, `endTime`, `reservationStatus`
- `PatientReservationPageResponse`: `content`, `page`, `size`, `totalElements`, `totalPages`
- `ReservationCancelResponse`: `reservationId`, `status`

주요 예외:

- 404 `PATIENT_001`: Patient 없음
- 404 `RESERVATION_004`: Schedule 없음
- 400 `RESERVATION_001`: Schedule이 AVAILABLE이 아님
- 404 `RESERVATION_005`: 본인 소유 예약 없음
- 400 `RESERVATION_002`: 완료 예약 취소
- 400 `RESERVATION_007`: 이미 취소된 예약

## 9. 의사 예약 API

Controller: `com.medflow.reservation.controller.DoctorReservationController`  
클래스 권한: DOCTOR

| Method | Path | 요청 | 응답 DTO | 인증 | Role |
|---|---|---|---|---|---|
| GET | `/api/v1/doctors/reservations` | 없음 | `List<DoctorReservationResponse>` | 필요 | DOCTOR |
| GET | `/api/v1/doctors/reservations/today` | 없음 | `List<DoctorReservationResponse>` | 필요 | DOCTOR |
| GET | `/api/v1/doctors/reservations/date` | Query `date: LocalDate` | `List<DoctorReservationResponse>` | 필요 | DOCTOR |
| GET | `/api/v1/doctors/reservations/search` | Query `date?`, `status?`, Pageable | `DoctorReservationPageResponse` | 필요 | DOCTOR |
| GET | `/api/v1/doctors/reservations/{reservationId}/patient` | Path `reservationId` | `DoctorReservationPatientResponse` | 필요 | DOCTOR |
| PATCH | `/api/v1/doctors/reservations/{reservationId}/complete` | Path `reservationId` | `ReservationCompleteResponse` | 필요 | DOCTOR |
| PATCH | `/api/v1/doctors/reservations/{reservationId}/approve` | Path `reservationId` | `ReservationDoctorApproveRejectResponse` | 필요 | DOCTOR |
| PATCH | `/api/v1/doctors/reservations/{reservationId}/reject` | Path `reservationId` | `ReservationDoctorApproveRejectResponse` | 필요 | DOCTOR |

DTO:

- `DoctorReservationResponse`: `reservationId`, `patientName`, `reservationDate`, `startTime`, `endTime`, `reservationStatus`
- `DoctorReservationPageResponse`: `content`, `page`, `size`, `totalElements`, `totalPages`; 기본 size 10
- `DoctorReservationPatientResponse`: 환자 ID/이름/성별/생년월일/전화번호 + 예약 ID/날짜/시간/상태
- `ReservationCompleteResponse`: `reservationId`, `reservationStatus`
- `ReservationDoctorApproveRejectResponse`: `reservationId`, `status`

주요 예외:

- 404 `DOCTOR_002`: 로그인 User에 연결된 Doctor 없음
- 404 `RESERVATION_005`: 예약 없음 또는 다른 의사의 예약
- 404 `PATIENT_001`: 예약에 Patient 없음
- 409 `RESERVATION_003`: 허용되지 않은 상태 전이

## 10. 관리자 예약 API

Controller: `com.medflow.reservation.controller.AdminReservationController`

| Method | Path | 요청 | 응답 DTO | 인증 | Role |
|---|---|---|---|---|---|
| GET | `/api/v1/admin/reservations` | Query filters + Pageable | `AdminReservationPageResponse` | 필요 | ADMIN |

Query: `hospitalId?`, `doctorId?`, `patientId?`, `date?`, `status?`, Pageable(기본 size 20).

- `AdminReservationResponse`: 예약 ID, 병원 ID/명, 의사 ID/명, 환자 ID/명, 예약 날짜/시간/상태, `createdAt`
- `AdminReservationPageResponse`: `content`, `page`, `size`, `totalElements`, `totalPages`

주요 예외: 403 `AUTH_006`, 400 `VALIDATION_ERROR`(날짜/Enum/숫자 query 변환 실패), 그 외 QueryDSL 조회는 빈 페이지를 반환할 수 있다.

## 11. 환자 문진 API

Controller: `com.medflow.questionnaire.controller.QuestionnaireController`  
클래스 권한: PATIENT

| Method | Path | 요청 DTO | 응답 DTO | 인증 | Role |
|---|---|---|---|---|---|
| POST | `/api/v1/questionnaires` | `QuestionnaireCreateRequest` | `QuestionnaireResponse` | 필요 | PATIENT |
| GET | `/api/v1/questionnaires/{reservationId}` | Path `reservationId` | `QuestionnaireDetailResponse` | 필요 | PATIENT |
| PUT | `/api/v1/questionnaires/{questionnaireId}` | `QuestionnaireUpdateRequest` | `QuestionnaireUpdateResponse` | 필요 | PATIENT |
| GET | `/api/v1/questionnaires/{questionnaireId}/analysis` | Path `questionnaireId` | `QuestionnaireAnalysisDetailResponse` | 필요 | PATIENT |

문진 요청 필드:

- 생성: `reservationId`, `chiefComplaint`, `symptomStartedAt`, `symptomDescription`, `painLevel`, `temperature`, `associatedSymptoms`, `medicalHistory`, `medications`, `allergies`, `additionalNote`
- 수정: 생성 필드에서 `reservationId` 제외

응답:

- `QuestionnaireResponse`: `questionnaireId`, `reservationId`, `patientId`, 문진 입력 필드 전체, `createdAt`
- `QuestionnaireDetailResponse`: `questionnaireId`, `reservationId`, `patientId`, 문진 입력 필드 전체, `createdAt`, `updatedAt`
- `QuestionnaireUpdateResponse`: `questionnaireId`, `reservationId`, `patientId`, 문진 입력 필드 전체, `updatedAt`
- `QuestionnaireAnalysisDetailResponse`: `analysisId`, `questionnaireId`, `summary`, `keyFindings`, `riskSignals`, `doctorCheckpoints`, `priorityLevel`, `status`

주요 예외:

- 404 `PATIENT_001`: Patient 없음
- 404 `RESERVATION_005`: Reservation 없음
- 409 `QUESTIONNAIRE_001`: 예약에 이미 문진 존재
- 403 `QUESTIONNAIRE_002`: 다른 환자의 예약/문진
- 400 `QUESTIONNAIRE_003`: 취소 예약
- 400 `QUESTIONNAIRE_004`: 완료 예약
- 404 `QUESTIONNAIRE_005`: 문진 없음
- 400 `QUESTIONNAIRE_006`: 진료 시작 후 수정
- 404 `QUESTIONNAIRE_007`: 분석 없음

## 12. 의사 문진 분석 API

Controller: `com.medflow.questionnaire.controller.DoctorQuestionnairesController`  
클래스 권한: DOCTOR

| Method | Path | 요청 | 응답 DTO | 인증 | Role |
|---|---|---|---|---|---|
| GET | `/api/v1/doctors/questionnaires/{questionnaireId}/analysis` | Path `questionnaireId` | `DoctorQuestionnaireAnalysisResponse` | 필요 | DOCTOR |

응답 필드: `analysisId`, `questionnaireId`, `reservationId`, `summary`, `keyFindings`, `riskSignals`, `doctorCheckpoints`, `priorityLevel`, `status`.

주요 예외:

- 404 `DOCTOR_002`: Doctor 없음
- 404 `QUESTIONNAIRE_005`: 문진 없음
- 403 `QUESTIONNAIRE_002`: 다른 의사가 담당한 예약의 문진
- 404 `QUESTIONNAIRE_007`: 분석 없음

## 13. ErrorCode 전체 목록

| HTTP | 코드 | 의미 |
|---:|---|---|
| 409 | `AUTH_001` | 이메일 중복 |
| 401 | `AUTH_002` | 잘못된 비밀번호 |
| 404 | `AUTH_003` | 사용자 없음 |
| 401 | `AUTH_004` | 토큰 권한 정보 없음 |
| 401 | `AUTH_005` | 인증 정보 오류 |
| 403 | `AUTH_006` | 접근 권한 없음 |
| 401 | `AUTH_007` | 인증 필요 |
| 409 | `AUTH_008` | 가입 불가 역할 |
| 404 | `PATIENT_001` | 환자 없음 |
| 409 | `PATIENT_002` | 환자 프로필 중복 |
| 409 | `HOSPITAL_001` | 병원 중복 |
| 409 | `HOSPITAL_002` | 병원 없음 |
| 409 | `DOCTOR_001` | 의사/면허 신청 중복 |
| 404 | `DOCTOR_002` | 의사 없음 |
| 400 | `DOCTOR_003` | 의사 상태 부적합 |
| 409 | `DOCTOR_004` | 면허번호 중복 |
| 403 | `DOCTOR_005` | 승인되지 않은 의사 |
| 400 | `RESERVATION_001` | 예약 불가 스케줄 |
| 400 | `RESERVATION_002` | 완료 예약 취소 |
| 409 | `RESERVATION_003` | 예약 상태 변경 불가 |
| 404 | `RESERVATION_004` | 스케줄 없음 |
| 404 | `RESERVATION_005` | 예약 없음 |
| 400 | `RESERVATION_007` | 이미 취소된 예약 |
| 409 | `QUESTIONNAIRE_001` | 문진 중복 |
| 403 | `QUESTIONNAIRE_002` | 문진 예약 접근 불가 |
| 400 | `QUESTIONNAIRE_003` | 취소 예약 문진 불가 |
| 400 | `QUESTIONNAIRE_004` | 완료 예약 문진 불가 |
| 404 | `QUESTIONNAIRE_005` | 문진 없음 |
| 400 | `QUESTIONNAIRE_006` | 진료 시작 후 수정 불가 |
| 404 | `QUESTIONNAIRE_007` | 문진 분석 없음 |
| 500 | `AI_001` | AI 분석 실패 |
| 500 | `AI_002` | Gemini API Key 미설정 |

`RESERVATION_006`은 정의되어 있지 않다. `INVALID_PASSWORD(AUTH_002)`와 `WithdrawRequest`는 현재 Controller 실행 경로에서 사용되지 않는다.

## 코드로 확인한 내용

- Controller에 선언된 44개 API의 Method, 정확한 path, 요청/응답 DTO
- SecurityConfig와 `@PreAuthorize`를 합친 인증/Role 정책
- DTO의 실제 필드와 validation 적용 여부
- 공통 성공/실패 응답 구조
- ErrorCode별 HTTP 상태와 GlobalExceptionHandler 매핑
- 현재 모든 정상 Controller 응답이 HTTP 200인 상태

## 작성자 확인이 필요한 내용

- [작성자 확인 필요: API 서버 Host와 환경별 base URL]
- [작성자 확인 필요: trailing slash를 포함한 현재 경로를 외부 계약으로 유지할지]
- [작성자 확인 필요: `/doctor`, `/admin/doctors` 경로를 `/api/v1`로 통합할지]
- [작성자 확인 필요: 생성/삭제 API의 목표 HTTP status 정책]
- [작성자 확인 필요: `/reissue`의 Access Token 인증 요구가 의도된 것인지]
- [작성자 확인 필요: 환자 프로필 API의 목표 Role 범위]
- [작성자 확인 필요: 병원 미존재가 409로 정의된 것이 의도된 것인지]
- [작성자 확인 필요: 문진 DTO의 공식 JSON 예시와 의료 데이터 필드별 허용 범위]
