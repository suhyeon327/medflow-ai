package com.medflow.doctor.dto.response;

import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.entity.DoctorStatus;

public record AdminDoctorDetailResponse(
        Long doctorId,
        String doctorName,
        String licenseNumber,
        Long hospitalId,
        String hospitalName,
        String email,
        String specialty,
        String introduction,
        String contact,
        DoctorStatus status
) {

    public static AdminDoctorDetailResponse from(Doctor doctor) {
        return new AdminDoctorDetailResponse(
                doctor.getId(),
                doctor.getName(),
                doctor.getLicenseNumber(),
                doctor.getHospital().getId(),
                doctor.getHospital().getName(),
                doctor.getUser().getEmail(),
                doctor.getSpecialty(),
                doctor.getIntroduction(),
                doctor.getContact(),
                doctor.getStatus()
        );
    }
}
