package com.medflow.doctor.service;

import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import com.medflow.doctor.dto.request.DoctorApplyRequest;
import com.medflow.doctor.dto.response.DoctorApplyResponse;
import com.medflow.doctor.dto.response.DoctorInfoResponse;
import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.repository.DoctorRepository;
import com.medflow.hospital.entity.Hospital;
import com.medflow.hospital.repository.HospitalRepository;
import com.medflow.user.entity.User;
import com.medflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final HospitalRepository hospitalRepository;

    // 의사 등록 신청
    public DoctorApplyResponse apply(Long userId, DoctorApplyRequest request) {

        // 회원 중복 여부 확인
        if (doctorRepository.findByUserId(userId).isPresent()) {
            throw new BusinessException(ErrorCode.DOCTOR_ALREADY_EXISTS);
        }

        // 등록된 면허번호인지 확인
        if (doctorRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new BusinessException(ErrorCode.DOCTOR_ALREADY_EXISTS);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Hospital hospital = hospitalRepository.findById(request.getHospitalId())
                .orElseThrow(() -> new BusinessException(ErrorCode.HOSPITAL_NOT_FOUND));

        Doctor doctor = Doctor.create(
                user,
                hospital,
                request.getName(),
                request.getLicenseNumber()
        );

        doctorRepository.save(doctor);

        return DoctorApplyResponse.from(doctor);
    }

    // 의사 프로필 조회
    @Transactional(readOnly = true)
    public DoctorInfoResponse getMyDoctorInfo(Long userId) {

        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCTOR_NOT_FOUND));

        return DoctorInfoResponse.from(doctor);
    }
}
