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

    // Patient
    PATIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PATIENT_001", "환자를 찾을 수 없습니다."),
    PATIENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "PATIENT_002", "이미 환자 정보가 존재합니다."),

    // Hospital
    HOSPITAL_ALREADY_EXISTS(HttpStatus.CONFLICT, "HOSPITAL_001", "이미 존재하는 병원입니다."),
    HOSPITAL_NOT_FOUND(HttpStatus.CONFLICT, "HOSPITAL_002", "병원을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
