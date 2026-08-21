package com.medflow.hospital.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AdminHospitalCreateRequest(

    @NotBlank(message = "이름은 필수입니다.")
    String name,

    @NotBlank(message = "주소는 필수입니다.")
    String address,

    @NotBlank(message = "지역은 필수입니다.")
    String region,

    @NotBlank
    String tel
) {
}
