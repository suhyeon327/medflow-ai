package com.medflow.hospital.dto.response;

import com.medflow.hospital.entity.Hospital;

public record HospitalDetailResponse(
        Long id,
        String name,
        String address,
        String region,
        String tel
) {

    public static HospitalDetailResponse from(Hospital hospital) {

        return new HospitalDetailResponse(
                hospital.getId(),
                hospital.getName(),
                hospital.getAddress(),
                hospital.getRegion(),
                hospital.getTel()
        );
    }
}
