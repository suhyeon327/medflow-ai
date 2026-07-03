package com.medflow.common.exception;

public class EmailAlreadyExistsException extends BusinessException {

    public EmailAlreadyExistsException() {
        super(ErrorCode.EMAIL_ALREADY_EXISTS);
    }
}
