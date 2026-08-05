package com.medflow.doctor.service;

import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import com.medflow.doctor.dto.response.AvailableDoctorScheduleResponse;
import com.medflow.doctor.dto.response.DoctorDetailResponse;
import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.entity.DoctorSchedule;
import com.medflow.doctor.entity.DoctorScheduleStatus;
import com.medflow.doctor.entity.DoctorStatus;
import com.medflow.doctor.repository.DoctorRepository;
import com.medflow.doctor.repository.DoctorScheduleRepository;
import com.medflow.user.entity.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicDoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;

    public DoctorDetailResponse getDoctor(Long doctorId) {
        return DoctorDetailResponse.from(getActiveDoctor(doctorId));
    }

    public List<AvailableDoctorScheduleResponse> getAvailableDoctorSchedules(
            Long doctorId,
            LocalDate date
    ) {
        getActiveDoctor(doctorId);

        List<DoctorSchedule> schedules = date == null
                ? doctorScheduleRepository.findByDoctorIdAndStatus(
                        doctorId,
                        DoctorScheduleStatus.AVAILABLE
                )
                : doctorScheduleRepository.findByDoctorIdAndStatusAndDate(
                        doctorId,
                        DoctorScheduleStatus.AVAILABLE,
                        date
                );

        return schedules
                .stream()
                .map(AvailableDoctorScheduleResponse::from)
                .toList();
    }

    private Doctor getActiveDoctor(Long doctorId) {
        return doctorRepository.findByIdAndStatusAndUserStatus(
                        doctorId,
                        DoctorStatus.ACTIVE,
                        UserStatus.ACTIVE
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCTOR_NOT_FOUND));
    }
}
