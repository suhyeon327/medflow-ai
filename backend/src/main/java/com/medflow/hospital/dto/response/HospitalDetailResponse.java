package com.medflow.hospital.dto.response;

import com.medflow.hospital.entity.Hospital;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HospitalDetailResponse {
    private Long id;
    private String name;
    private String address;
    private String region;
    private String tel;

    public static HospitalDetailResponse from(Hospital hospital) {

        return HospitalDetailResponse.builder()
                .id(hospital.getId())
                .name(hospital.getName())
                .address(hospital.getAddress())
                .region(hospital.getRegion())
                .tel(hospital.getTel())
                .build();
    }
}
