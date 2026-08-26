package com.medflow.reservation.service;

import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import com.medflow.doctor.entity.DoctorSchedule;
import com.medflow.doctor.entity.DoctorScheduleStatus;
import com.medflow.doctor.repository.DoctorScheduleRepository;
import com.medflow.patient.entity.Patient;
import com.medflow.patient.repository.PatientRepository;
import com.medflow.questionnaire.entity.Questionnaire;
import com.medflow.questionnaire.repository.QuestionnaireRepository;
import com.medflow.reservation.dto.request.ReservationCreateRequest;
import com.medflow.reservation.dto.response.PatientReservationResponse;
import com.medflow.reservation.dto.response.ReservationCancelResponse;
import com.medflow.reservation.dto.response.ReservationCreateResponse;
import com.medflow.reservation.entity.Reservation;
import com.medflow.reservation.entity.ReservationPeriod;
import com.medflow.reservation.entity.ReservationStatus;
import com.medflow.reservation.repository.ReservationRepository;
import com.medflow.reservation.repository.ReservationSearchRepository;
import com.medflow.reservation.dto.response.PatientReservationPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final PatientRepository patientRepository;
    private final ReservationSearchRepository reservationSearchRepository;
    private final QuestionnaireRepository questionnaireRepository;
    
    // 환자 예약 내역 조회
    @Transactional(readOnly = true)
    public PatientReservationPageResponse getPatientReservations(Long userId, ReservationStatus status, LocalDate date, Long hospitalId, Long doctorId, ReservationPeriod period, Pageable pageable) {

        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PATIENT_NOT_FOUND));

        Page<Reservation> reservationPage = reservationSearchRepository
                .search(patient.getId(), status, date, hospitalId, doctorId, period, pageable);
        Map<Long, Long> questionnaireIdByReservationId = getQuestionnaireIdByReservationId(
                reservationPage.getContent()
        );
        Page<PatientReservationResponse> responsePage = reservationPage.map(reservation ->
                PatientReservationResponse.from(
                        reservation,
                        questionnaireIdByReservationId.get(reservation.getId())
                )
        );

        return PatientReservationPageResponse.from(responsePage);
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

        List<Reservation> reservations = reservationRepository.findByPatientId(patient.getId());
        Map<Long, Long> questionnaireIdByReservationId = getQuestionnaireIdByReservationId(reservations);

        return reservations.stream()
                .map(reservation -> PatientReservationResponse.from(
                        reservation,
                        questionnaireIdByReservationId.get(reservation.getId())
                ))
                .toList();
    }

    private Map<Long, Long> getQuestionnaireIdByReservationId(List<Reservation> reservations) {
        List<Long> reservationIds = reservations.stream()
                .map(Reservation::getId)
                .toList();

        if (reservationIds.isEmpty()) {
            return Map.of();
        }

        return questionnaireRepository.findAllByReservationIdIn(reservationIds).stream()
                .collect(Collectors.toMap(
                        questionnaire -> questionnaire.getReservation().getId(),
                        Questionnaire::getId
                ));
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
