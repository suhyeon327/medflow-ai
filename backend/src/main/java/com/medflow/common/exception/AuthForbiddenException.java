package com.medflow.common.exception;

public class AuthForbiddenException extends BusinessException {

    public AuthForbiddenException() {
        super(ErrorCode.AUTH_FORBIDDEN);
    }
}
