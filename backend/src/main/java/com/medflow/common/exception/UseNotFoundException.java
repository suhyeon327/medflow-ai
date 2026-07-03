package com.medflow.common.exception;

public class UseNotFoundException extends BusinessException {

    public UseNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}
