package com.medflow.reservation.service;

import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import com.medflow.doctor.entity.DoctorSchedule;
import com.medflow.doctor.entity.DoctorScheduleStatus;
import com.medflow.doctor.repository.DoctorScheduleRepository;
import com.medflow.patient.entity.Patient;
import com.medflow.patient.repository.PatientRepository;
import com.medflow.reservation.dto.request.ReservationCreateRequest;
import com.medflow.reservation.dto.response.PatientReservationResponse;
import com.medflow.reservation.dto.response.ReservationCancelResponse;
import com.medflow.reservation.dto.response.ReservationCreateResponse;
import com.medflow.reservation.entity.Reservation;
import com.medflow.reservation.entity.ReservationPeriod;
import com.medflow.reservation.entity.ReservationStatus;
import com.medflow.reservation.repository.ReservationRepository;
import com.medflow.reservation.repository.PatientReservationSearchRepository;
import com.medflow.reservation.dto.response.PatientReservationPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final PatientRepository patientRepository;
    private final PatientReservationSearchRepository patientReservationSearchRepository;
    
    // 환자 예약 내역 조회
    @Transactional(readOnly = true)
    public PatientReservationPageResponse getPatientReservations(Long userId, ReservationStatus status, LocalDate date, Long hospitalId, Long doctorId, ReservationPeriod period, Pageable pageable) {

        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PATIENT_NOT_FOUND));

        Page<PatientReservationResponse> reservationPage = patientReservationSearchRepository
                .search(patient.getId(), status, date, hospitalId, doctorId, period, pageable)
                .map(PatientReservationResponse::from);

        return PatientReservationPageResponse.from(reservationPage);
    }

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

    // 환자 예약 내역 조회
    @Transactional(readOnly = true)
    public List<PatientReservationResponse> getPatientReservations(
            Long userId
    ) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PATIENT_NOT_FOUND));

        return reservationRepository
                .findByPatientId(patient.getId())
                .stream()
                .map(PatientReservationResponse::from)
                .toList();
    }

    // 환자 예약 취소
    public ReservationCancelResponse cancelReservation(Long userId, Long reservationId) {

        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PATIENT_NOT_FOUND));

        Reservation reservation = reservationRepository
                .findByIdAndPatientId(reservationId, patient.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        reservation.cancel();
        reservation.getDoctorSchedule().release();

        return ReservationCancelResponse.from(reservation);
    }
}
