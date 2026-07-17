package com.medflow.doctor.dto.response;

import com.medflow.doctor.entity.Doctor;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DoctorDeleteResponse {

    @NotNull
    private Long doctorId;

    private String message;

    public static DoctorDeleteResponse from(Doctor doctor) {
        return DoctorDeleteResponse.builder()
                .doctorId(doctor.getId())
                .message("의사 인증 신청이 취소되었습니다.")
                .build();
    }
}
