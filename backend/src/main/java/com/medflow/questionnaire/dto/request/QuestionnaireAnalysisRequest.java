package com.medflow.questionnaire.dto.request;

import com.medflow.questionnaire.entity.Questionnaire;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record QuestionnaireAnalysisRequest(
        Long questionnaireId,
        String chiefComplaint,
        LocalDateTime symptomStartedAt,
        String symptomDescription,
        Integer painLevel,
        BigDecimal temperature,
        String associatedSymptoms,
        String medicalHistory,
        String medications,
        String allergies,
        String additionalNote
) {
    public static QuestionnaireAnalysisRequest from(Questionnaire questionnaire) {
        return new QuestionnaireAnalysisRequest(
                questionnaire.getId(),
                questionnaire.getChiefComplaint(),
                questionnaire.getSymptomStartedAt(),
                questionnaire.getSymptomDescription(),
                questionnaire.getPainLevel(),
                questionnaire.getTemperature(),
                questionnaire.getAssociatedSymptoms(),
                questionnaire.getMedicalHistory(),
                questionnaire.getMedications(),
                questionnaire.getAllergies(),
                questionnaire.getAdditionalNote()
        );
    }
}
