package com.medflow.common.exception;

public class HospitalAlreadyExistsException extends BusinessException {

    public HospitalAlreadyExistsException() {
        super(ErrorCode.HOSPITAL_ALREADY_EXISTS);
    }
}
