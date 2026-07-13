package com.medflow.patient.service;

import com.medflow.patient.dto.AdminPatientResponse;
import com.medflow.patient.dto.PatientRequest;
import com.medflow.patient.dto.PatientResponse;
import com.medflow.user.entity.User;

import java.util.List;

public interface PatientService {

    PatientResponse createPatient(Long userId, PatientRequest request);

    PatientResponse getPatientProfile(Long userId);

    PatientResponse updatePatient(Long userId, PatientRequest request);

    List<AdminPatientResponse> getPatients();
}
