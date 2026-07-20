package com.medflow.doctor.dto.response;

import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.entity.DoctorStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DoctorListResponse {
    private Long doctorId;

    private String doctorName;

    private String hospitalName;

    private String licenseNumber;

    private DoctorStatus status;


    public static DoctorListResponse from(Doctor doctor) {

        return DoctorListResponse.builder()
                .doctorId(doctor.getId())
                .doctorName(doctor.getName())
                .hospitalName(doctor.getHospital().getName())
                .licenseNumber(doctor.getLicenseNumber())
                .status(doctor.getStatus())
                .build();
    }
}
