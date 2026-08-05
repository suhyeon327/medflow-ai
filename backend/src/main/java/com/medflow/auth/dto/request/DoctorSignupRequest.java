package com.medflow.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
public record DoctorSignupRequest(
        @NotNull(message = "병원 ID는 필수입니다.") Long hospitalId,
        @NotBlank(message = "의사 이름은 필수입니다.") @Size(max = 50) String name,
        @NotBlank(message = "면허번호는 필수입니다.") @Size(max = 30) String licenseNumber,
        @NotBlank(message = "전문과목은 필수입니다.") @Size(max = 100) String specialty,
        @Size(max = 1000) String introduction,
        @Size(max = 20) String contact
) {
}
