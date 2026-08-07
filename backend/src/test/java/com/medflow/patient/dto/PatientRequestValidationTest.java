package com.medflow.patient.dto;

import com.medflow.patient.entity.Gender;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PatientRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void 정상_환자_수정_요청은_Validation을_통과한다() {
        // given
        PatientRequest request = new PatientRequest(
                "홍길동", LocalDate.of(1999, 5, 20), Gender.MALE, "01012345678"
        );

        // when
        var violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    void 필수값이_없으면_Validation에_실패한다() {
        // given
        PatientRequest request = new PatientRequest(" ", null, null, " ");

        // when
        var violations = validator.validate(request);

        // then
        assertThat(violations).extracting(violation -> violation.getPropertyPath().toString())
                .contains("name", "birth", "gender", "phone");
    }

    @Test
    void 전화번호가_10자리_미만이면_Validation에_실패한다() {
        // given
        PatientRequest request = new PatientRequest(
                "홍길동", LocalDate.of(1999, 5, 20), Gender.MALE, "010123456"
        );

        // when
        var violations = validator.validate(request);

        // then
        assertThat(violations).anySatisfy(violation -> {
            assertThat(violation.getPropertyPath().toString()).isEqualTo("phone");
            assertThat(violation.getMessage()).isEqualTo("전화번호 형식이 올바르지 않습니다.");
        });
    }

    @Test
    void 전화번호에_하이픈이_포함되면_Validation에_실패한다() {
        // given
        PatientRequest request = new PatientRequest(
                "홍길동", LocalDate.of(1999, 5, 20), Gender.MALE, "010-1234-5678"
        );

        // when
        var violations = validator.validate(request);

        // then
        assertThat(violations).extracting(violation -> violation.getPropertyPath().toString())
                .contains("phone");
    }
}
