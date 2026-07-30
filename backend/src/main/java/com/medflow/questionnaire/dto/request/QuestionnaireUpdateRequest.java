package com.medflow.questionnaire.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record QuestionnaireUpdateRequest(
        @NotBlank(message = "주 증상은 필수입니다.") String chiefComplaint,
        @NotNull(message = "증상 시작 시점은 필수입니다.") LocalDateTime symptomStartedAt,
        @NotBlank(message = "증상 상세 설명은 필수입니다.") String symptomDescription,
        @Min(value = 0, message = "통증 정도는 0 이상이어야 합니다.")
        @Max(value = 10, message = "통증 정도는 10 이하여야 합니다.") Integer painLevel,
        BigDecimal temperature,
        String associatedSymptoms,
        String medicalHistory,
        String medications,
        String allergies,
        String additionalNote
) {
}
