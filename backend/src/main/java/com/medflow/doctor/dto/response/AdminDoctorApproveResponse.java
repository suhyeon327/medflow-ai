package com.medflow.doctor.dto.response;

import com.medflow.doctor.entity.Doctor;

public record AdminDoctorApproveResponse(
        Long doctorId,
        String message
) {

    public static AdminDoctorApproveResponse from(Doctor doctor) {
        return new AdminDoctorApproveResponse(doctor.getId(), "의사 인증되었습니다.");
    }
}
