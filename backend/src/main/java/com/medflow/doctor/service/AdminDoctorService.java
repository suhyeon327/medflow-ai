package com.medflow.doctor.service;

import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import com.medflow.doctor.dto.response.AdminDoctorApproveResponse;
import com.medflow.doctor.dto.response.AdminDoctorDetailResponse;
import com.medflow.doctor.dto.response.AdminDoctorRejectResponse;
import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@PreAuthorize("hasRole('ADMIN')")
public class AdminDoctorService {

    private final DoctorRepository doctorRepository;

    @Transactional(readOnly = true)
    public AdminDoctorDetailResponse getDoctorDetail(Long doctorId) {
        return AdminDoctorDetailResponse.from(getDoctor(doctorId));
    }

    public AdminDoctorApproveResponse approveDoctor(Long doctorId) {
        Doctor doctor = getDoctor(doctorId);
        doctor.approve();
        return AdminDoctorApproveResponse.from(doctor);
    }

    public AdminDoctorRejectResponse rejectDoctor(Long doctorId) {
        Doctor doctor = getDoctor(doctorId);
        doctor.reject();
        return AdminDoctorRejectResponse.from(doctor);
    }

    private Doctor getDoctor(Long doctorId) {
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCTOR_NOT_FOUND));
    }
}
