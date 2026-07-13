package com.medflow.patient.service;

import com.medflow.common.exception.PatientAlreadyExistsException;
import com.medflow.common.exception.PatientNotFoundException;
import com.medflow.common.exception.UserNotFoundException;
import com.medflow.patient.dto.AdminPatientResponse;
import com.medflow.patient.dto.PatientRequest;
import com.medflow.patient.dto.PatientResponse;
import com.medflow.patient.entity.Patient;
import com.medflow.patient.repository.PatientRepository;
import com.medflow.user.entity.User;
import com.medflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    // 환자 등록
    @Override
    public PatientResponse createPatient(Long userId, PatientRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 이미 생성된 환자인지 확인
        if (patientRepository.existsByUserId(userId)) {
            throw new PatientAlreadyExistsException();
        }

        Patient patient = Patient.create(
                user,
                request.getName(),
                request.getBirth(),
                request.getGender(),
                request.getPhone()
        );

        Patient savedPatient = patientRepository.save(patient);

        return PatientResponse.from(savedPatient);
    }

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
    public PatientResponse updatePatient(Long userId, PatientRequest request) {

        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(PatientNotFoundException::new);

        patient.update(
                request.getName(),
                request.getBirth(),
                request.getGender(),
                request.getPhone()
        );

        return PatientResponse.from(patient);
    }

    // 전체 환자 조회
    public List<AdminPatientResponse> getPatients() {

        return patientRepository.findAll()
                .stream()
                .map(AdminPatientResponse::from)
                .toList();
    }
}
