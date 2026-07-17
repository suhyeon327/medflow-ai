package com.medflow.doctor.service;

import com.medflow.doctor.dto.response.PendingDoctorResponse;
import com.medflow.doctor.entity.DoctorStatus;
import com.medflow.doctor.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@PreAuthorize("hasRole('ADMIN')")
public class DoctorAdminService {

    private final DoctorRepository doctorRepository;

    // 승인 대기 의사 목록 조회
    public List<PendingDoctorResponse> getPendingDoctors() {

        return doctorRepository.findAllByStatus(DoctorStatus.PENDING)
                .stream()
                .map(PendingDoctorResponse::from)
                .toList();
    }
}
