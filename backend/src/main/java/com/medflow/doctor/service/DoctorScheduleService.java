package com.medflow.doctor.service;

import com.medflow.doctor.dto.response.DoctorScheduleResponse;
import com.medflow.doctor.entity.DoctorScheduleStatus;
import com.medflow.doctor.repository.DoctorScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DoctorScheduleService {

    private final DoctorScheduleRepository doctorScheduleRepository;

    // 예약 가능한 시간 조회
    @Transactional(readOnly = true)
    public List<DoctorScheduleResponse> getAvailableSchedules(Long doctorId) {
        return doctorScheduleRepository.findByDoctorIdAndStatus(doctorId, DoctorScheduleStatus.AVAILABLE)
                .stream()
                .map(DoctorScheduleResponse::from)
                .toList();
    }
}
