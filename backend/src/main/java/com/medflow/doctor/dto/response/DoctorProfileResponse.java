package com.medflow.doctor.dto.response;

import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.entity.DoctorStatus;

public record DoctorProfileResponse(
        Long doctorId,
        String doctorName,
        String licenseNumber,
        Long hospitalId,
        String hospitalName,
        String specialty,
        String introduction,
        String contact,
        DoctorStatus status
) {

    public static DoctorProfileResponse from(Doctor doctor) {
        return new DoctorProfileResponse(
                doctor.getId(),
                doctor.getName(),
                doctor.getLicenseNumber(),
                doctor.getHospital().getId(),
                doctor.getHospital().getName(),
                doctor.getSpecialty(),
                doctor.getIntroduction(),
                doctor.getContact(),
                doctor.getStatus()
        );
    }
}
