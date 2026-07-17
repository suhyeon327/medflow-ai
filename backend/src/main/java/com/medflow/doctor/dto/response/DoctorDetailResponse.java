package com.medflow.doctor.dto.response;

import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.entity.DoctorStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DoctorDetailResponse {

    private Long doctorId;

    private String doctorName;

    private String licenseNumber;

    private Long hospitalId;

    private String hospitalName;

    private String email;

    private DoctorStatus status;


    public static DoctorDetailResponse from(Doctor doctor) {

        return DoctorDetailResponse.builder()
                .doctorId(doctor.getId())
                .doctorName(doctor.getName())
                .licenseNumber(doctor.getLicenseNumber())
                .hospitalId(doctor.getHospital().getId())
                .hospitalName(doctor.getHospital().getName())
                .email(doctor.getUser().getEmail())
                .status(doctor.getStatus())
                .build();
    }
}
