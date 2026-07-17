package com.medflow.doctor.service;

import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import com.medflow.doctor.dto.response.DoctorApproveResponse;
import com.medflow.doctor.dto.response.DoctorDetailResponse;
import com.medflow.doctor.dto.response.DoctorRejectResponse;
import com.medflow.doctor.dto.response.DoctorStatusResponse;
import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.entity.DoctorStatus;
import com.medflow.doctor.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@PreAuthorize("hasRole('ADMIN')")
public class DoctorAdminService {

    private final DoctorRepository doctorRepository;

    // 의사 상태 조회
    @Transactional(readOnly = true)
    public List<DoctorStatusResponse> getDoctorsByStatus(
            DoctorStatus status
    ) {
        return doctorRepository.findAllByStatus(status)
                .stream()
                .map(DoctorStatusResponse::from)
                .toList();
    }

    // 의사 인증 신청 상세 조회
    @Transactional(readOnly = true)
    public DoctorDetailResponse getDoctorDetail(Long doctorId) {

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCTOR_NOT_FOUND));


        return DoctorDetailResponse.from(doctor);
    }

    // 의사 승인
    public DoctorApproveResponse approveDoctor(Long doctorId) {

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCTOR_NOT_FOUND));

        doctor.approve();

        return DoctorApproveResponse.from(doctor);
    }

    // 의사 반려
    public DoctorRejectResponse rejectDoctor(Long doctorId) {

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCTOR_NOT_FOUND));

        doctor.reject();

        return DoctorRejectResponse.from(doctor);
    }
}
