package com.medflow.questionnaire.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class QuestionnaireRequestValidationTest {

    private static final int TEXT_MAX_LENGTH = 10000;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void 생성_요청_문자열이_최대_길이면_Validation을_통과한다() {
        var violations = validator.validate(createRequest(200, TEXT_MAX_LENGTH));

        assertThat(violations).isEmpty();
    }

    @Test
    void 생성_요청_문자열이_최대_길이를_초과하면_Validation에_실패한다() {
        var violations = validator.validate(createRequest(201, TEXT_MAX_LENGTH + 1));

        assertThat(violations).extracting(violation -> violation.getPropertyPath().toString())
                .contains(
                        "chiefComplaint",
                        "symptomDescription",
                        "associatedSymptoms",
                        "medicalHistory",
                        "medications",
                        "allergies",
                        "additionalNote"
                );
    }

    @Test
    void 수정_요청_문자열이_최대_길이면_Validation을_통과한다() {
        var violations = validator.validate(updateRequest(200, TEXT_MAX_LENGTH));

        assertThat(violations).isEmpty();
    }

    @Test
    void 수정_요청_문자열이_최대_길이를_초과하면_Validation에_실패한다() {
        var violations = validator.validate(updateRequest(201, TEXT_MAX_LENGTH + 1));

        assertThat(violations).extracting(violation -> violation.getPropertyPath().toString())
                .contains(
                        "chiefComplaint",
                        "symptomDescription",
                        "associatedSymptoms",
                        "medicalHistory",
                        "medications",
                        "allergies",
                        "additionalNote"
                );
    }

    private QuestionnaireCreateRequest createRequest(int chiefComplaintLength, int textLength) {
        String text = "가".repeat(textLength);
        return new QuestionnaireCreateRequest(
                1L,
                "가".repeat(chiefComplaintLength),
                LocalDateTime.of(2026, 9, 16, 9, 0),
                text,
                5,
                null,
                text,
                text,
                text,
                text,
                text
        );
    }

    private QuestionnaireUpdateRequest updateRequest(int chiefComplaintLength, int textLength) {
        String text = "가".repeat(textLength);
        return new QuestionnaireUpdateRequest(
                "가".repeat(chiefComplaintLength),
                LocalDateTime.of(2026, 9, 16, 9, 0),
                text,
                5,
                null,
                text,
                text,
                text,
                text,
                text
        );
    }
}
