package com.medflow.patient.dto;

import com.medflow.patient.entity.Gender;
import com.medflow.patient.entity.Patient;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class PatientResponse {
    private Long id;
    private String name;
    private LocalDate birth;
    private Gender gender;
    private String phone;

    public static PatientResponse from(Patient patient) {

        return PatientResponse.builder()
                .id(patient.getId())
                .name(patient.getName())
                .birth(patient.getBirth())
                .gender(patient.getGender())
                .phone(patient.getPhone())
                .build();
    }

}
