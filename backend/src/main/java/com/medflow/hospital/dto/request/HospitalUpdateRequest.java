package com.medflow.hospital.dto.request;

import com.medflow.hospital.entity.HospitalStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HospitalUpdateRequest {

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotBlank(message = "주소는 필수입니다.")
    private String address;

    @NotBlank(message = "지역은 필수입니다.")
    private String region;

    @NotBlank
    private String tel;

    @NotNull
    private HospitalStatus status;
}
