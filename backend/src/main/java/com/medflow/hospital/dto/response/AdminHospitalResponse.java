package com.medflow.hospital.dto.response;

import com.medflow.hospital.entity.Hospital;
import com.medflow.hospital.entity.HospitalStatus;

import java.time.LocalDateTime;

public record AdminHospitalResponse(
        Long id,
        String name,
        String address,
        String region,
        String tel,
        HospitalStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
) {

    public static AdminHospitalResponse from(Hospital hospital) {

        return new AdminHospitalResponse(
                hospital.getId(),
                hospital.getName(),
                hospital.getAddress(),
                hospital.getRegion(),
                hospital.getTel(),
                hospital.getStatus(),
                hospital.getCreatedAt(),
                hospital.getUpdatedAt(),
                hospital.getDeletedAt()
        );
    }
}
