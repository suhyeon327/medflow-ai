package com.medflow.patient.service;

import com.medflow.common.exception.PatientNotFoundException;
import com.medflow.patient.dto.PatientRequest;
import com.medflow.patient.dto.PatientResponse;
import com.medflow.patient.entity.Patient;
import com.medflow.patient.repository.PatientRepository;
import com.medflow.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    // 환자 등록
    @Override
    public void registerPatient(User user, PatientRequest request) {

        Patient patient = Patient.create(
                user,
                request.getName(),
                request.getBirth(),
                request.getGender(),
                request.getPhone()
        );

        patientRepository.save(patient);
    }

    // 환자 정보 생성
    @Override
    @Transactional(readOnly = true)
    public PatientResponse getMyProfile(User user) {

        return PatientResponse.from(findPatient(user));
    }

    // 환자 정보 수정
    @Override
    public void update(User user, PatientRequest request) {

        findPatient(user).update(
                request.getName(),
                request.getBirth(),
                request.getGender(),
                request.getPhone()
        );

    }

    // 환자 가져오기
    public Patient findPatient(User user) {

        return patientRepository.findByUser(user)
                .orElseThrow(PatientNotFoundException::new);
    }
}
