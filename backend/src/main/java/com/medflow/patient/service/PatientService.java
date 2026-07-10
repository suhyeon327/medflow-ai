package com.medflow.patient.service;

import com.medflow.patient.dto.PatientRequest;
import com.medflow.patient.dto.PatientResponse;
import com.medflow.user.entity.User;

import java.util.List;

public interface PatientService {

    void registerPatient(User user, PatientRequest request);

    PatientResponse getMyProfile(User user);

    void update(User user, PatientRequest request);

//    PatientResponse getPatient(Long patientId);

//    List<PatientResponse> getPatients();
}
