package com.medflow.questionnaire.dto.response;

import com.medflow.questionnaire.entity.Questionnaire;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record QuestionnaireDetailResponse(
        Long questionnaireId,
        Long reservationId,
        Long patientId,
        String chiefComplaint,
        LocalDateTime symptomStartedAt,
        String symptomDescription,
        Integer painLevel,
        BigDecimal temperature,
        String associatedSymptoms,
        String medicalHistory,
        String medications,
        String allergies,
        String additionalNote,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static QuestionnaireDetailResponse from(Questionnaire questionnaire) {
        return new QuestionnaireDetailResponse(
                questionnaire.getId(),
                questionnaire.getReservation().getId(),
                questionnaire.getReservation().getPatient().getId(),
                questionnaire.getChiefComplaint(),
                questionnaire.getSymptomStartedAt(),
                questionnaire.getSymptomDescription(),
                questionnaire.getPainLevel(),
                questionnaire.getTemperature(),
                questionnaire.getAssociatedSymptoms(),
                questionnaire.getMedicalHistory(),
                questionnaire.getMedications(),
                questionnaire.getAllergies(),
                questionnaire.getAdditionalNote(),
                questionnaire.getCreatedAt(),
                questionnaire.getUpdatedAt()
        );
    }
}
