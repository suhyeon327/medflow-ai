package com.medflow.patient.dto;

import com.medflow.patient.entity.Gender;
import com.medflow.patient.entity.Patient;

import java.time.LocalDate;

public record PatientResponse(
        Long id,
        String name,
        LocalDate birth,
        Gender gender,
        String phone
) {

    public static PatientResponse from(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getName(),
                patient.getBirth(),
                patient.getGender(),
                patient.getPhone()
        );
    }
}
