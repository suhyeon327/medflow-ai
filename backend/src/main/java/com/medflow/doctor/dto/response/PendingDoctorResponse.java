package com.medflow.doctor.dto.response;

import com.medflow.doctor.entity.Doctor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PendingDoctorResponse {

    private Long doctorId;

    private String doctorName;

    private String hospitalName;

    private String licenseNumber;

    public static PendingDoctorResponse from(Doctor doctor) {

        return PendingDoctorResponse.builder()
                .doctorId(doctor.getId())
                .doctorName(doctor.getName())
                .hospitalName(doctor.getHospital().getName())
                .licenseNumber(doctor.getLicenseNumber())
                .build();
    }
}
