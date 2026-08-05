package com.medflow.reservation.service;

import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.repository.DoctorRepository;
import com.medflow.questionnaire.repository.QuestionnaireRepository;
import com.medflow.questionnaire.repository.QuestionnaireAnalysisRepository;
import com.medflow.reservation.dto.response.DoctorReservationResponse;
import com.medflow.reservation.dto.response.DoctorReservationPatientResponse;
import com.medflow.reservation.dto.response.DoctorReservationPageResponse;
import com.medflow.reservation.dto.response.ReservationStatusResponse;
import com.medflow.reservation.entity.Reservation;
import com.medflow.reservation.entity.ReservationStatus;
import com.medflow.reservation.repository.ReservationRepository;
import com.medflow.reservation.repository.DoctorReservationSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class DoctorReservationService {

    private final ReservationRepository reservationRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorReservationSearchRepository doctorReservationSearchRepository;
    private final QuestionnaireRepository questionnaireRepository;
    private final QuestionnaireAnalysisRepository questionnaireAnalysisRepository;

    // 의사 예약 검색 및 필터링
    @Transactional(readOnly = true)
    public DoctorReservationPageResponse getDoctorReservations(
            Long userId,
            LocalDate date,
            ReservationStatus status,
            Pageable pageable
    ) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCTOR_NOT_FOUND));

        Page<DoctorReservationResponse> reservationPage = doctorReservationSearchRepository
                .search(doctor.getId(), date, status, pageable)
                .map(DoctorReservationResponse::from);

        return DoctorReservationPageResponse.from(reservationPage);
    }

    // 환자 정보 조회
    @Transactional(readOnly = true)
    public DoctorReservationPatientResponse getReservationPatient(Long userId, Long reservationId) {

        Reservation reservation = findDoctorReservation(userId, reservationId);

        if (reservation.getPatient() == null) {
            throw new BusinessException(ErrorCode.PATIENT_NOT_FOUND);
        }

        return DoctorReservationPatientResponse.from(reservation);
    }

    // 예약 상태 변경
    public ReservationStatusResponse updateReservationStatus(
            Long userId,
            Long reservationId,
            ReservationStatus status
    ) {
        Reservation reservation = findDoctorReservation(userId, reservationId);

        switch (status) {
            case APPROVED -> reservation.approve();
            case REJECTED -> {
                reservation.reject();
                reservation.getDoctorSchedule().release();
            }
            case COMPLETED -> reservation.complete();
            default -> throw new BusinessException(ErrorCode.INVALID_STATUS_CHANGE);
        }

        return ReservationStatusResponse.from(reservation);
    }

    private Reservation findDoctorReservation(Long userId, Long reservationId) {

        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCTOR_NOT_FOUND));

        return reservationRepository.findByIdAndDoctorScheduleDoctorId(reservationId, doctor.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
    }
}
