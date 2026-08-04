package com.medflow.hospital.dto.response;

import com.medflow.hospital.entity.Hospital;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HospitalListResponse {
    private Long id;
    private String name;
    private String region;
    private String address;

    public static HospitalListResponse from(Hospital hospital) {

        return HospitalListResponse.builder()
                .id(hospital.getId())
                .name(hospital.getName())
                .region(hospital.getRegion())
                .address(hospital.getAddress())
                .build();
    }
}
