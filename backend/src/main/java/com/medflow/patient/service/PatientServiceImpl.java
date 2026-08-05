package com.medflow.patient.service;

import com.medflow.common.exception.PatientNotFoundException;
import com.medflow.patient.dto.PatientRequest;
import com.medflow.patient.dto.PatientResponse;
import com.medflow.patient.entity.Patient;
import com.medflow.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    // 환자 정보 조회
    @Override
    @Transactional(readOnly = true)
    public PatientResponse getPatientProfile(Long userId) {

        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(PatientNotFoundException::new);

        return PatientResponse.from(patient);
    }

    // 환자 정보 수정
    @Override
    public PatientResponse updatePatientProfile(Long userId, PatientRequest request) {

        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(PatientNotFoundException::new);

        patient.update(
                request.name(),
                request.birth(),
                request.gender(),
                request.phone()
        );

        return PatientResponse.from(patient);
    }
}
