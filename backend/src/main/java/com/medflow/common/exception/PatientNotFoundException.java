package com.medflow.common.exception;

public class PatientNotFoundException extends BusinessException {

    public PatientNotFoundException() {
        super(ErrorCode.PATIENT_NOT_FOUND);
    }
}
