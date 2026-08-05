package com.medflow.auth.dto.request;

import com.medflow.user.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
public record SignupRequest(
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @Size(max = 100)
        String email,
        @NotBlank(message = "비밀번호는 8~20자입니다.")
        @Size(min = 8, max = 20)
        String password,
        @NotNull(message = "회원 유형을 선택해주세요.")
        UserRole role,
        @Valid PatientSignupRequest patient,
        @Valid DoctorSignupRequest doctor
) {
}
