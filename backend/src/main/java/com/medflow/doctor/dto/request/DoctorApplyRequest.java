package com.medflow.doctor.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DoctorApplyRequest {

    @NotNull
    private Long hospitalId;

    @NotBlank
    private String name;

    @NotBlank
    private String licenseNumber;
}
