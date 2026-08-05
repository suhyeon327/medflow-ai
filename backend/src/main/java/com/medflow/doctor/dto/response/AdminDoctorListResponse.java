package com.medflow.doctor.dto.response;

import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.entity.DoctorStatus;

public record AdminDoctorListResponse(
        Long doctorId,
        String doctorName,
        String hospitalName,
        String licenseNumber,
        DoctorStatus status
) {

    public static AdminDoctorListResponse from(Doctor doctor) {
        return new AdminDoctorListResponse(
                doctor.getId(),
                doctor.getName(),
                doctor.getHospital().getName(),
                doctor.getLicenseNumber(),
                doctor.getStatus()
        );
    }
}