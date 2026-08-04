package com.medflow.doctor.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @Size(max = 100)
    private String specialty;

    @Size(max = 1000)
    private String introduction;

    @Size(max = 20)
    private String contact;
}
