package com.medflow.reservation.service;

import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.entity.DoctorSchedule;
import com.medflow.doctor.entity.DoctorScheduleStatus;
import com.medflow.doctor.repository.DoctorScheduleRepository;
import com.medflow.patient.entity.Patient;
import com.medflow.patient.repository.PatientRepository;
import com.medflow.reservation.dto.request.ReservationCreateRequest;
import com.medflow.reservation.dto.response.ReservationCreateResponse;
import com.medflow.reservation.entity.Reservation;
import com.medflow.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final PatientRepository patientRepository;

    // 예약 생성
    public ReservationCreateResponse createReservation(Long userId, ReservationCreateRequest request) {

        // 예약하려는 시간 정보 가져오기
        DoctorSchedule doctorschedule = doctorScheduleRepository.findById(request.scheduleId())
                .orElseThrow(() -> new BusinessException((ErrorCode.SCHEDULE_NOT_FOUND)));

        // 예약이 가능한지 확인
        if (doctorschedule.getStatus() != DoctorScheduleStatus.AVAILABLE) {
            throw new BusinessException(ErrorCode.SCHEDULE_NOT_AVAILABLE);
        }

        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PATIENT_NOT_FOUND));

        Reservation reservation = Reservation.create(patient, doctorschedule);

        doctorschedule.reserve();

        reservationRepository.save(reservation);

        return ReservationCreateResponse.from(reservation);

    }
}
