package com.medflow.hospital.dto.request;

import com.medflow.hospital.entity.HospitalStatus;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdminHospitalRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void 생성_요청_문자열이_컬럼_최대_길이면_Validation을_통과한다() {
        AdminHospitalCreateRequest request = new AdminHospitalCreateRequest(
                "가".repeat(100),
                "가".repeat(255),
                "가".repeat(50),
                "1".repeat(20)
        );

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void 생성_요청_문자열이_컬럼_최대_길이를_초과하면_Validation에_실패한다() {
        AdminHospitalCreateRequest request = new AdminHospitalCreateRequest(
                "가".repeat(101),
                "가".repeat(256),
                "가".repeat(51),
                "1".repeat(21)
        );

        assertInvalidStringFields(request);
    }

    @Test
    void 수정_요청_문자열이_컬럼_최대_길이면_Validation을_통과한다() {
        AdminHospitalUpdateRequest request = new AdminHospitalUpdateRequest(
                "가".repeat(100),
                "가".repeat(255),
                "가".repeat(50),
                "1".repeat(20),
                HospitalStatus.ACTIVE
        );

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void 수정_요청_문자열이_컬럼_최대_길이를_초과하면_Validation에_실패한다() {
        AdminHospitalUpdateRequest request = new AdminHospitalUpdateRequest(
                "가".repeat(101),
                "가".repeat(256),
                "가".repeat(51),
                "1".repeat(21),
                HospitalStatus.ACTIVE
        );

        assertInvalidStringFields(request);
    }

    private void assertInvalidStringFields(Object request) {
        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("name", "address", "region", "tel");
    }
}
