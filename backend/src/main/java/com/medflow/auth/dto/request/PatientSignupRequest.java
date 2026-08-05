package com.medflow.auth.dto.request;

import com.medflow.patient.entity.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record PatientSignupRequest(
        @NotBlank(message = "환자 이름은 필수입니다.")
        @Size(max = 50)
        String name,
        @NotNull(message = "생년월일은 필수입니다.") LocalDate birth,
        @NotNull(message = "성별은 필수입니다.") Gender gender,
        @NotBlank(message = "전화번호는 필수입니다.")
        @Pattern(regexp = "^\\d{3}-?\\d{3,4}-?\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
        String phone
) {
}
