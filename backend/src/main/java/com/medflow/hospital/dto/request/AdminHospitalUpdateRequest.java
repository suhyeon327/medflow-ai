package com.medflow.hospital.dto.request;

import com.medflow.hospital.entity.HospitalStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminHospitalUpdateRequest(

        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 100, message = "병원 이름은 100자 이하여야 합니다.")
        String name,

        @NotBlank(message = "주소는 필수입니다.")
        @Size(max = 255, message = "주소는 255자 이하여야 합니다.")
        String address,

        @NotBlank(message = "지역은 필수입니다.")
        @Size(max = 50, message = "지역은 50자 이하여야 합니다.")
        String region,

        @NotBlank
        @Size(max = 20, message = "전화번호는 20자 이하여야 합니다.")
        String tel,

        @NotNull
        HospitalStatus status
) {
}
