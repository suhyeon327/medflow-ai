package com.medflow.doctor.service;

import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import com.medflow.doctor.dto.request.DoctorScheduleCreateRequest;
import com.medflow.doctor.dto.response.DoctorScheduleResponse;
import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.entity.DoctorSchedule;
import com.medflow.doctor.entity.DoctorScheduleStatus;
import com.medflow.doctor.entity.DoctorStatus;
import com.medflow.doctor.repository.DoctorRepository;
import com.medflow.doctor.repository.DoctorScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DoctorScheduleService {

    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;

    // 의사 진료 스케줄 등록
    public List<DoctorScheduleResponse> createSchedule(
            Long userId,
            DoctorScheduleCreateRequest request
    ) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCTOR_NOT_FOUND));

        if(doctor.getStatus() != DoctorStatus.ACTIVE){
            throw new BusinessException(ErrorCode.DOCTOR_NOT_APPROVED);
        }

        List<DoctorScheduleResponse> responses = new ArrayList<>();

        LocalTime start = request.startTime();

        while (start.isBefore(request.endTime())) {

            LocalTime next = start.plusMinutes(request.slotMinutes());

            if (next.isAfter(request.endTime())) {
                break;
            }

            boolean exists = doctorScheduleRepository.existsByDoctorIdAndDateAndStartTime(
                    doctor.getId(),
                    request.date(),
                    start
            );

            if (!exists) {
                DoctorSchedule doctorSchedule = DoctorSchedule.create(doctor, request.date(), start, next);

                DoctorSchedule savedSchedule = doctorScheduleRepository.save(doctorSchedule);

                responses.add(DoctorScheduleResponse.from(savedSchedule));
            }

            start = next;
        }

        return responses;
    }

    // 예약 가능 시간 조회
    @Transactional(readOnly = true)
    public List<DoctorScheduleResponse> getAvailableSchedules(Long doctorId) {

        return doctorScheduleRepository.findByDoctorIdAndStatus(doctorId, DoctorScheduleStatus.AVAILABLE)
                .stream()
                .map(DoctorScheduleResponse::from)
                .toList();
    }
}
