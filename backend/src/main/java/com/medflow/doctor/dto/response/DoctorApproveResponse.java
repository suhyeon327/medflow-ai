package com.medflow.doctor.dto.response;

import com.medflow.doctor.entity.Doctor;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DoctorApproveResponse {

    @NotNull
    private Long doctorId;

    private String message;

    public static DoctorApproveResponse from(Doctor doctor) {
        return DoctorApproveResponse.builder()
                .doctorId(doctor.getId())
                .message("의사 인증되었습니다.")
                .build();
    }
}
