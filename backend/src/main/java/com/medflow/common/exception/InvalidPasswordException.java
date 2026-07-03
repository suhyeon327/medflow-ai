package com.medflow.common.exception;

public class InvalidPasswordException extends BusinessException  {

    public InvalidPasswordException() {
        super(ErrorCode.INVALID_PASSWORD);
    }
}