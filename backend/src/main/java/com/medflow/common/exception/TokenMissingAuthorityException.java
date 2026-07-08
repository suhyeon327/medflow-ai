package com.medflow.common.exception;

public class TokenMissingAuthorityException extends BusinessException {

    public TokenMissingAuthorityException() {
        super(ErrorCode.TOKEN_MISSING_AUTHORITY);
    }
}