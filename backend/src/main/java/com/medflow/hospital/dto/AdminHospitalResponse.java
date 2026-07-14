package com.medflow.hospital.dto;

import com.medflow.hospital.entity.Hospital;
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
                .createdAt(hospital.getCreatedAt())
                .updatedAt(hospital.getUpdatedAt())
                .deletedAt(hospital.getDeletedAt())
                .build();
    }
}
