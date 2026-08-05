package com.medflow.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Auth
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH_001", "이미 가입된 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "AUTH_002", "비밀번호가 올바르지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_003", "사용자를 찾을 수 없습니다."),
    TOKEN_MISSING_AUTHORITY(HttpStatus.UNAUTHORIZED, "AUTH_004", "권한 정보가 없는 토큰입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_005", "이메일 또는 비밀번호가 올바르지 않습니다."),
    AUTH_FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_006", "접근 권한이 없습니다."),
    AUTH_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_007", "인증이 필요합니다."),
    INVALID_SIGNUP_ROLE(HttpStatus.CONFLICT, "AUTH_008", "회원가입 가능한 회원 유형이 아닙니다."),
    INVALID_SIGNUP_PROFILE(HttpStatus.BAD_REQUEST, "AUTH_009", "회원 유형에 맞는 추가 정보를 입력해주세요."),

    // Patient
    PATIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PATIENT_001", "환자를 찾을 수 없습니다."),
    PATIENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "PATIENT_002", "이미 환자 정보가 존재합니다."),

    // Hospital
    HOSPITAL_ALREADY_EXISTS(HttpStatus.CONFLICT, "HOSPITAL_001", "이미 존재하는 병원입니다."),
    HOSPITAL_NOT_FOUND(HttpStatus.CONFLICT, "HOSPITAL_002", "병원을 찾을 수 없습니다."),

    // Doctor
    DOCTOR_ALREADY_EXISTS(HttpStatus.CONFLICT, "DOCTOR_001", "이미 의사 인증을 신청했거나 등록된 면허번호입니다."),
    DOCTOR_NOT_FOUND(HttpStatus.NOT_FOUND, "DOCTOR_002", "의사 정보를 찾을 수 없습니다."),
    INVALID_DOCTOR_STATUS(HttpStatus.BAD_REQUEST, "DOCTOR_003", "현재 상태에서는 요청을 처리할 수 없습니다."),
    LICENSE_NUMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "DOCTOR_004", "이미 등록된 면허번호입니다."),
    DOCTOR_NOT_APPROVED(HttpStatus.FORBIDDEN, "DOCTOR_005", "승인되지 않은 의사는 해당 기능을 사용할 수 없습니다."),

    // Reservation
    SCHEDULE_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "RESERVATION_001", "예약할 수 없는 시간입니다."),
    COMPLETED_RESERVATION(HttpStatus.BAD_REQUEST, "RESERVATION_002", "진료가 완료된 예약은 취소할 수 없습니다."),
    INVALID_STATUS_CHANGE(HttpStatus.CONFLICT, "RESERVATION_003", "취소된 예약은 상태 변경 불가합니다."),
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "RESERVATION_004", "예약 가능한 일정을 찾을 수 없습니다."),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "RESERVATION_005", "예약을 찾을 수 없습니다."),
    RESERVATION_ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "RESERVATION_007", "이미 취소된 예약입니다."),

    // Questionnaire
    QUESTIONNAIRE_ALREADY_EXISTS(HttpStatus.CONFLICT, "QUESTIONNAIRE_001", "해당 예약에 이미 문진이 작성되었습니다."),
    QUESTIONNAIRE_RESERVATION_FORBIDDEN(HttpStatus.FORBIDDEN, "QUESTIONNAIRE_002", "본인의 예약에만 문진을 작성할 수 있습니다."),
    QUESTIONNAIRE_CANCELLED_RESERVATION(HttpStatus.BAD_REQUEST, "QUESTIONNAIRE_003", "취소된 예약에는 문진을 작성할 수 없습니다."),
    QUESTIONNAIRE_COMPLETED_RESERVATION(HttpStatus.BAD_REQUEST, "QUESTIONNAIRE_004", "진료가 완료된 예약에는 문진을 작성할 수 없습니다."),
    QUESTIONNAIRE_NOT_FOUND(HttpStatus.NOT_FOUND, "QUESTIONNAIRE_005", "문진을 찾을 수 없습니다."),
    QUESTIONNAIRE_UPDATE_AFTER_START(HttpStatus.BAD_REQUEST, "QUESTIONNAIRE_006", "진료가 시작된 예약의 문진은 수정할 수 없습니다."),
    QUESTIONNAIRE_ANALYSIS_NOT_FOUND(HttpStatus.NOT_FOUND, "QUESTIONNAIRE_007", "문진 분석 내용을 찾을 수 없습니다."),

    // AI
    AI_ANALYSIS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI_001", "AI 문진 분석 중 오류가 발생했습니다."),
    GEMINI_API_KEY_NOT_CONFIGURED(HttpStatus.INTERNAL_SERVER_ERROR, "AI_002", "Gemini API Key가 설정되지 않았습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
