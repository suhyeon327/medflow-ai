package com.medflow.hospital.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HospitalRequest {

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotBlank(message = "주소는 필수입니다.")
    private String address;

    @NotBlank(message = "지역은 필수입니다.")
    private String region;

    @NotBlank
    private String tel;
}
