package com.medflow.patient.dto;

import com.medflow.patient.entity.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record PatientRequest(

    @NotBlank(message = "이름은 필수입니다.")
    String name,

    @NotNull(message = "생년월일은 필수입니다.")
    LocalDate birth,

    @NotNull(message = "성별은 필수입니다.")
    Gender gender,

    @NotBlank
    @Pattern(
            regexp = "^\\d{10,11}$",
            message = "전화번호 형식이 올바르지 않습니다."
    )
    String phone
) {
}
