package com.medflow.common.exception;

public class PatientAlreadyExistsException extends BusinessException  {

    public PatientAlreadyExistsException() {
        super(ErrorCode.PATIENT_ALREADY_EXISTS);
    }
}