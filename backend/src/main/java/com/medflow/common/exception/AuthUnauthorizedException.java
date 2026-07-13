package com.medflow.common.exception;

public class AuthUnauthorizedException extends BusinessException {

    public AuthUnauthorizedException() {
        super(ErrorCode.AUTH_UNAUTHORIZED);
    }
}
