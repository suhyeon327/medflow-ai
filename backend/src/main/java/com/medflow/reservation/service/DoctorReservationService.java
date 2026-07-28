package com.medflow.reservation.service;

import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.repository.DoctorRepository;
import com.medflow.doctor.repository.DoctorScheduleRepository;
import com.medflow.patient.repository.PatientRepository;
import com.medflow.reservation.dto.response.ReservationDoctorApproveRejectResponse;
import com.medflow.reservation.entity.Reservation;
import com.medflow.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DoctorReservationService {

    private final ReservationRepository reservationRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    // 예약 승인
    public ReservationDoctorApproveRejectResponse approveReservation(Long userId, Long reservationId) {

        Reservation reservation = findDoctorRequestedReservation(userId, reservationId);
        reservation.approve();

        return ReservationDoctorApproveRejectResponse.from(reservation);
    }

    // 예약 취소
    public ReservationDoctorApproveRejectResponse rejectReservation(Long userId, Long reservationId) {

        Reservation reservation = findDoctorRequestedReservation(userId, reservationId);
        reservation.reject();
        reservation.getDoctorSchedule().release();

        return ReservationDoctorApproveRejectResponse.from(reservation);
    }

    private Reservation findDoctorRequestedReservation(Long userId, Long reservationId) {

        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCTOR_NOT_FOUND));

        return reservationRepository.findByIdAndDoctorScheduleDoctorId(reservationId, doctor.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
    }
}
