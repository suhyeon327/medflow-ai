package com.medflow.hospital.dto;

import com.medflow.hospital.entity.Hospital;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HospitalListResponse {
    private Long id;
    private String name;

    public static HospitalListResponse from(Hospital hospital) {

        return HospitalListResponse.builder()
                .id(hospital.getId())
                .name(hospital.getName())
                .build();
    }
}
