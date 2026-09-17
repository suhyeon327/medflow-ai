package com.medflow.questionnaire.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record QuestionnaireUpdateRequest(
        @NotBlank(message = "주 증상은 필수입니다.")
        @Size(max = 200, message = "주 증상은 200자 이하여야 합니다.") String chiefComplaint,
        @NotNull(message = "증상 시작 시점은 필수입니다.") LocalDateTime symptomStartedAt,
        @NotBlank(message = "증상 상세 설명은 필수입니다.")
        @Size(max = 10000, message = "증상 상세 설명은 10000자 이하여야 합니다.") String symptomDescription,
        @Min(value = 0, message = "통증 정도는 0 이상이어야 합니다.")
        @Max(value = 10, message = "통증 정도는 10 이하여야 합니다.") Integer painLevel,
        BigDecimal temperature,
        @Size(max = 10000, message = "동반 증상은 10000자 이하여야 합니다.") String associatedSymptoms,
        @Size(max = 10000, message = "기저질환은 10000자 이하여야 합니다.") String medicalHistory,
        @Size(max = 10000, message = "복용 중인 약은 10000자 이하여야 합니다.") String medications,
        @Size(max = 10000, message = "알레르기는 10000자 이하여야 합니다.") String allergies,
        @Size(max = 10000, message = "추가 전달사항은 10000자 이하여야 합니다.") String additionalNote
) {
}
