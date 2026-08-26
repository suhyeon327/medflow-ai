package com.medflow.hospital.dto.response;

import com.medflow.hospital.dto.projection.HospitalDoctorProjection;
import com.medflow.hospital.entity.Hospital;

import java.util.List;
import java.util.Objects;

public record HospitalListResponse(
        Long id,
        String name,
        String region,
        String address,
        String tel,
        int doctorCount,
        List<String> specialties
) {

    public static HospitalListResponse from(
            Hospital hospital,
            List<HospitalDoctorProjection> doctors
    ) {
        List<String> specialties = doctors.stream()
                .map(HospitalDoctorProjection::getSpecialty)
                .filter(Objects::nonNull)
                .filter(specialty -> !specialty.isBlank())
                .distinct()
                .toList();

        return new HospitalListResponse(
                hospital.getId(),
                hospital.getName(),
                hospital.getRegion(),
                hospital.getAddress(),
                hospital.getTel(),
                doctors.size(),
                specialties
        );
    }
}