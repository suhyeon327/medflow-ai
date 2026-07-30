package com.medflow.questionnaire.service;

import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import com.medflow.patient.entity.Patient;
import com.medflow.patient.repository.PatientRepository;
import com.medflow.questionnaire.dto.request.QuestionnaireCreateRequest;
import com.medflow.questionnaire.dto.request.QuestionnaireUpdateRequest;
import com.medflow.questionnaire.dto.response.QuestionnaireDetailResponse;
import com.medflow.questionnaire.dto.response.QuestionnaireResponse;
import com.medflow.questionnaire.dto.response.QuestionnaireUpdateResponse;
import com.medflow.questionnaire.entity.Questionnaire;
import com.medflow.questionnaire.entity.QuestionnaireAnalysis;
import com.medflow.questionnaire.event.QuestionnaireAnalysisRequestedEvent;
import com.medflow.questionnaire.repository.QuestionnaireAnalysisRepository;
import com.medflow.questionnaire.repository.QuestionnaireRepository;
import com.medflow.reservation.entity.Reservation;
import com.medflow.reservation.entity.ReservationStatus;
import com.medflow.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionnaireService {

    private final QuestionnaireRepository questionnaireRepository;
    private final QuestionnaireAnalysisRepository questionnaireAnalysisRepository;
    private final ReservationRepository reservationRepository;
    private final PatientRepository patientRepository;
    private final ApplicationEventPublisher eventPublisher;

    // 예약 문진 작성
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

        Questionnaire savedQuestionnaire = questionnaireRepository.save(questionnaire);
        questionnaireAnalysisRepository.save(QuestionnaireAnalysis.pending(savedQuestionnaire));
        eventPublisher.publishEvent(new QuestionnaireAnalysisRequestedEvent(savedQuestionnaire.getId()));

        return QuestionnaireResponse.from(savedQuestionnaire);
    }

    // 예약 문진 조회
    @Transactional(readOnly = true)
    public QuestionnaireDetailResponse getQuestionnaire(Long userId, Long reservationId) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PATIENT_NOT_FOUND));

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        validateReservationOwner(patient, reservation);

        Questionnaire questionnaire = questionnaireRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTIONNAIRE_NOT_FOUND));

        return QuestionnaireDetailResponse.from(questionnaire);
    }

    // 예약 문진 수정
    public QuestionnaireUpdateResponse updateQuestionnaire(
            Long userId,
            Long questionnaireId,
            QuestionnaireUpdateRequest request
    ) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PATIENT_NOT_FOUND));

        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTIONNAIRE_NOT_FOUND));

        Reservation reservation = questionnaire.getReservation();
        
        validateReservationOwner(patient, reservation);
        validateQuestionnaireUpdateTime(reservation);

        questionnaire.update(
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

        QuestionnaireAnalysis analysis =
                questionnaireAnalysisRepository.findByQuestionnaireId(questionnaireId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.QUESTIONNAIRE_ANALYSIS_NOT_FOUND));

        analysis.resetToPending();
        eventPublisher.publishEvent(new QuestionnaireAnalysisRequestedEvent(questionnaire.getId()));

        return QuestionnaireUpdateResponse.from(questionnaireRepository.saveAndFlush(questionnaire));
    }

    private void validateReservation(Patient patient, Reservation reservation) {
        validateReservationOwner(patient, reservation);

        // 취소된 예약인지 확인
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.QUESTIONNAIRE_CANCELLED_RESERVATION);
        }

        // 완료된 예약인지 확인
        if (reservation.getStatus() == ReservationStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.QUESTIONNAIRE_COMPLETED_RESERVATION);
        }
    }

    private void validateReservationOwner(Patient patient, Reservation reservation) {
        // 본인의 예약인지 확인
        if (!reservation.getPatient().getId().equals(patient.getId())) {
            throw new BusinessException(ErrorCode.QUESTIONNAIRE_RESERVATION_FORBIDDEN);
        }
    }

    private void validateQuestionnaireUpdateTime(Reservation reservation) {
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.QUESTIONNAIRE_CANCELLED_RESERVATION);
        }
        if (reservation.getStatus() == ReservationStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.QUESTIONNAIRE_COMPLETED_RESERVATION);
        }

        LocalDateTime appointmentStart = LocalDateTime.of(
                reservation.getDoctorSchedule().getDate(),
                reservation.getDoctorSchedule().getStartTime()
        );
        if (!LocalDateTime.now().isBefore(appointmentStart)) {
            throw new BusinessException(ErrorCode.QUESTIONNAIRE_UPDATE_AFTER_START);
        }
    }
}
