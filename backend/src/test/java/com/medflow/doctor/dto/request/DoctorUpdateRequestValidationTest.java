package com.medflow.doctor.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class DoctorUpdateRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void 문자열이_컬럼_최대_길이면_Validation을_통과한다() {
        DoctorUpdateRequest request = request(50, 30, 100, 1000, 20);

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void 문자열이_컬럼_최대_길이를_초과하면_Validation에_실패한다() {
        DoctorUpdateRequest request = request(51, 31, 101, 1001, 21);

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("name", "licenseNumber", "specialty", "introduction", "contact");
    }

    private DoctorUpdateRequest request(
            int nameLength,
            int licenseNumberLength,
            int specialtyLength,
            int introductionLength,
            int contactLength
    ) {
        DoctorUpdateRequest request = new DoctorUpdateRequest();
        ReflectionTestUtils.setField(request, "hospitalId", 1L);
        ReflectionTestUtils.setField(request, "name", "가".repeat(nameLength));
        ReflectionTestUtils.setField(request, "licenseNumber", "L".repeat(licenseNumberLength));
        ReflectionTestUtils.setField(request, "specialty", "가".repeat(specialtyLength));
        ReflectionTestUtils.setField(request, "introduction", "가".repeat(introductionLength));
        ReflectionTestUtils.setField(request, "contact", "1".repeat(contactLength));
        return request;
    }
}
