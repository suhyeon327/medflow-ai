package com.medflow.patient.service;

import com.medflow.patient.dto.PatientRequest;
import com.medflow.patient.dto.PatientResponse;

public interface PatientService {

    PatientResponse getPatientProfile(Long userId);

    PatientResponse updatePatientProfile(Long userId, PatientRequest request);
}
