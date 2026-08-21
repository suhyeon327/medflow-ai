package com.medflow.hospital.dto.request;

import com.medflow.hospital.entity.HospitalStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminHospitalUpdateRequest(

        @NotBlank(message = "이름은 필수입니다.")
        String name,

        @NotBlank(message = "주소는 필수입니다.")
        String address,

        @NotBlank(message = "지역은 필수입니다.")
        String region,

        @NotBlank
        String tel,

        @NotNull
        HospitalStatus status
) {
}
