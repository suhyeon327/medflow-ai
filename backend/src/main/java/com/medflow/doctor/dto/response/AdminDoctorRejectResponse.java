package com.medflow.doctor.dto.response;

import com.medflow.doctor.entity.Doctor;

public record AdminDoctorRejectResponse(
        Long doctorId,
        String message
) {

    public static AdminDoctorRejectResponse from(Doctor doctor) {
        return new AdminDoctorRejectResponse(doctor.getId(), "의사 승인이 반려되었습니다.");
    }
}
