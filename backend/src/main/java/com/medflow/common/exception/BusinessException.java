package com.medflow.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    private BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());   // RuntimeException(부모 클래스)에 에러 메시지를 전달하는 코드
        this.errorCode = errorCode;
    }
}
