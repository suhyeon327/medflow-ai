package com.medflow.patient.dto;

import com.medflow.patient.entity.Gender;
import com.medflow.patient.entity.Patient;
import com.medflow.user.entity.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class AdminPatientResponse {
    private Long patientId;
    private String name;
    private String email;
    private LocalDate birth;
    private Gender gender;
    private String phone;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public static AdminPatientResponse from(Patient patient) {
        return AdminPatientResponse.builder()
                .patientId(patient.getId())
                .name(patient.getName())
                .email(patient.getUser().getEmail())
                .birth(patient.getBirth())
                .gender(patient.getGender())
                .phone(patient.getPhone())
                .status(patient.getUser().getStatus())
                .createdAt(patient.getCreatedAt())
                .updatedAt(patient.getUpdatedAt())
                .deletedAt(patient.getDeletedAt())
                .build();
    }
}
