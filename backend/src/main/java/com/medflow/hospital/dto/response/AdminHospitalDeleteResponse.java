package com.medflow.hospital.dto.response;

import com.medflow.hospital.entity.Hospital;

import java.time.LocalDateTime;

public record AdminHospitalDeleteResponse(
        Long hospitalId,
        LocalDateTime deletedAt,
        String message
) {
    public static AdminHospitalDeleteResponse from(Hospital hospital) {
        return new AdminHospitalDeleteResponse(
                hospital.getId(),
                hospital.getDeletedAt(),
                "병원 삭제가 완료되었습니다."
        );
    }
}
