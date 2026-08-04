package com.medflow.doctor.dto.response;

import com.medflow.doctor.entity.Doctor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PublicDoctorResponse {

    private Long doctorId;
    private String doctorName;
    private Long hospitalId;
    private String hospitalName;

    public static PublicDoctorResponse from(Doctor doctor) {
        return PublicDoctorResponse.builder()
                .doctorId(doctor.getId())
                .doctorName(doctor.getName())
                .hospitalId(doctor.getHospital().getId())
                .hospitalName(doctor.getHospital().getName())
                .build();
    }
}
