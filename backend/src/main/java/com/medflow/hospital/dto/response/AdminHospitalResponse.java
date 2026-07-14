package com.medflow.hospital.dto.response;

import com.medflow.hospital.entity.Hospital;
import com.medflow.hospital.entity.HospitalStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminHospitalResponse {
    private Long id;
    private String name;
    private String address;
    private String region;
    private String tel;
    private HospitalStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public static AdminHospitalResponse from(Hospital hospital) {

        return AdminHospitalResponse.builder()
                .id(hospital.getId())
                .name(hospital.getName())
                .address(hospital.getAddress())
                .region(hospital.getRegion())
                .tel(hospital.getTel())
                .status(hospital.getStatus())
                .createdAt(hospital.getCreatedAt())
                .updatedAt(hospital.getUpdatedAt())
                .deletedAt(hospital.getDeletedAt())
                .build();
    }
}
