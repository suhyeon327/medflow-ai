package com.medflow.doctor.dto.response;

import com.medflow.doctor.entity.Doctor;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DoctorRejectResponse {

    @NotNull
    private Long doctorId;

    private String message;

    public static DoctorRejectResponse from(Doctor doctor) {
        return DoctorRejectResponse.builder()
                .doctorId(doctor.getId())
                .message("의사 승인이 반려되었습니다.")
                .build();
    }
}
