package com.medflow.hospital.dto;

import com.medflow.hospital.entity.Hospital;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HospitalResponse {
    private Long id;
    private String name;
    private String address;
    private String region;
    private String tel;

    public static HospitalResponse from(Hospital hospital) {

        return HospitalResponse.builder()
                .id(hospital.getId())
                .name(hospital.getName())
                .address(hospital.getAddress())
                .region(hospital.getRegion())
                .tel(hospital.getTel())
                .build();
    }
}
