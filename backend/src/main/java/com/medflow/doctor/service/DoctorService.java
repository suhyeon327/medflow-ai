package com.medflow.doctor.service;

import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import com.medflow.doctor.dto.request.DoctorScheduleCreateRequest;
import com.medflow.doctor.dto.request.DoctorUpdateRequest;
import com.medflow.doctor.dto.response.DoctorProfileResponse;
import com.medflow.doctor.dto.response.DoctorScheduleResponse;
import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.entity.DoctorSchedule;
import com.medflow.doctor.entity.DoctorStatus;
import com.medflow.doctor.repository.DoctorRepository;
import com.medflow.doctor.repository.DoctorScheduleRepository;
import com.medflow.hospital.entity.Hospital;
import com.medflow.hospital.repository.HospitalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final HospitalRepository hospitalRepository;

    @Transactional(readOnly = true)
    public DoctorProfileResponse getDoctorProfile(Long userId) {
        Doctor doctor = getDoctorByUserId(userId);
        return DoctorProfileResponse.from(doctor);
    }

    public DoctorProfileResponse updateDoctorProfile(Long userId, DoctorUpdateRequest request) {
        Doctor doctor = getDoctorByUserId(userId);

        Hospital hospital = hospitalRepository.findById(request.getHospitalId())
                .orElseThrow(() -> new BusinessException(ErrorCode.HOSPITAL_NOT_FOUND));

        if (!doctor.getLicenseNumber().equals(request.getLicenseNumber())
                && doctorRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new BusinessException(ErrorCode.LICENSE_NUMBER_ALREADY_EXISTS);
        }

        doctor.update(
                hospital,
                request.getName(),
                request.getLicenseNumber(),
                request.getSpecialty(),
                request.getIntroduction(),
                request.getContact()
        );

        return DoctorProfileResponse.from(doctor);
    }

    public List<DoctorScheduleResponse> createDoctorSchedules(
            Long userId,
            DoctorScheduleCreateRequest request
    ) {
        validateScheduleTimeRange(request);

        Doctor doctor = getDoctorByUserId(userId);

        if (doctor.getStatus() != DoctorStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.DOCTOR_NOT_APPROVED);
        }

        if (doctorScheduleRepository.existsOverlappingSchedule(
                doctor.getId(), request.date(), request.startTime(), request.endTime()
        )) {
            throw new BusinessException(ErrorCode.SCHEDULE_NOT_AVAILABLE);
        }

        List<DoctorScheduleResponse> responses = new ArrayList<>();
        LocalTime start = request.startTime();

        while (start.isBefore(request.endTime())) {
            LocalTime next = start.plusMinutes(request.slotMinutes());

            if (next.isAfter(request.endTime())) {
                break;
            }

            DoctorSchedule schedule = DoctorSchedule.create(doctor, request.date(), start, next);
            try {
                responses.add(DoctorScheduleResponse.from(doctorScheduleRepository.saveAndFlush(schedule)));
            } catch (DataIntegrityViolationException e) {
                throw new BusinessException(ErrorCode.SCHEDULE_NOT_AVAILABLE);
            }

            start = next;
        }

        return responses;
    }

    private void validateScheduleTimeRange(DoctorScheduleCreateRequest request) {
        if (request.startTime() == null
                || request.endTime() == null
                || !request.endTime().isAfter(request.startTime())) {
            throw new BusinessException(ErrorCode.INVALID_SCHEDULE_TIME_RANGE);
        }
    }

    @Transactional(readOnly = true)
    public List<DoctorScheduleResponse> getDoctorSchedules(Long userId, LocalDate date) {
        Doctor doctor = getDoctorByUserId(userId);

        List<DoctorSchedule> schedules = date == null
                ? doctorScheduleRepository.findByDoctorId(doctor.getId())
                : doctorScheduleRepository.findByDoctorIdAndDate(doctor.getId(), date);

        return schedules
                .stream()
                .map(DoctorScheduleResponse::from)
                .toList();
    }

    private Doctor getDoctorByUserId(Long userId) {
        return doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCTOR_NOT_FOUND));
    }
}
