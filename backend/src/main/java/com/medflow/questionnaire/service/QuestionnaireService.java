package com.medflow.questionnaire.service;

import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import com.medflow.patient.entity.Patient;
import com.medflow.patient.repository.PatientRepository;
import com.medflow.questionnaire.dto.request.QuestionnaireCreateRequest;
import com.medflow.questionnaire.dto.response.QuestionnaireResponse;
import com.medflow.questionnaire.entity.Questionnaire;
import com.medflow.questionnaire.repository.QuestionnaireRepository;
import com.medflow.reservation.entity.Reservation;
import com.medflow.reservation.entity.ReservationStatus;
import com.medflow.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionnaireService {

    private final QuestionnaireRepository questionnaireRepository;
    private final ReservationRepository reservationRepository;
    private final PatientRepository patientRepository;

    // 로그인한 환자의 예약에 문진 작성
    public QuestionnaireResponse createQuestionnaire(Long userId, QuestionnaireCreateRequest request) {
    
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PATIENT_NOT_FOUND));
                
        Reservation reservation = reservationRepository.findById(request.reservationId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        validateReservation(patient, reservation);

        // 문진이 완료된 예약인지 확인
        if (questionnaireRepository.existsByReservationId(reservation.getId())) {
            throw new BusinessException(ErrorCode.QUESTIONNAIRE_ALREADY_EXISTS);
        }

        Questionnaire questionnaire = Questionnaire.create(
                reservation,
                request.chiefComplaint(),
                request.symptomStartedAt(),
                request.symptomDescription(),
                request.painLevel(),
                request.temperature(),
                request.associatedSymptoms(),
                request.medicalHistory(),
                request.medications(),
                request.allergies(),
                request.additionalNote()
        );

        return QuestionnaireResponse.from(questionnaireRepository.save(questionnaire));
    }

    private void validateReservation(Patient patient, Reservation reservation) {

        // 본인의 예약인지 확인
        if (!reservation.getPatient().getId().equals(patient.getId())) {
            throw new BusinessException(ErrorCode.QUESTIONNAIRE_RESERVATION_FORBIDDEN);
        }

        // 취소된 예약인지 확인
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.QUESTIONNAIRE_CANCELLED_RESERVATION);
        }

        // 완료된 예약인지 확인
        if (reservation.getStatus() == ReservationStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.QUESTIONNAIRE_COMPLETED_RESERVATION);
        }
    }
}
