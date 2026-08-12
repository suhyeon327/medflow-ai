package com.medflow.hospital.dto.response;

import com.medflow.doctor.entity.Doctor;
import com.medflow.hospital.entity.Hospital;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

@Getter
@Builder
public class HospitalListResponse {

    private Long id;
    private String name;
    private String region;
    private String address;
    private String tel;
    private int doctorCount;
    private List<String> specialties;

    public static HospitalListResponse of(
            Hospital hospital,
            List<Doctor> doctors
    ) {
        List<String> specialties = doctors.stream()
                .map(Doctor::getSpecialty)
                .filter(Objects::nonNull)
                .filter(specialty -> !specialty.isBlank())
                .distinct()
                .toList();

        return HospitalListResponse.builder()
                .id(hospital.getId())
                .name(hospital.getName())
                .region(hospital.getRegion())
                .address(hospital.getAddress())
                .tel(hospital.getTel())
                .doctorCount(doctors.size())
                .specialties(specialties)
                .build();
    }
}