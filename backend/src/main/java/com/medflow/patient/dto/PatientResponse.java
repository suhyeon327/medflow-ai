package com.medflow.patient.dto;

import com.medflow.patient.entity.Gender;
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
}
