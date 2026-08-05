package com.medflow.auth.dto.response;

import com.medflow.user.entity.UserRole;
import com.medflow.doctor.entity.DoctorStatus;
import com.medflow.doctor.entity.Doctor;
import com.medflow.patient.entity.Patient;
import com.medflow.user.entity.User;

public record SignupResponse(
        Long id,
        String email,
        UserRole role,
        Long profileId,
        DoctorStatus profileStatus
) {
    public static SignupResponse from(User user, Patient patient) {
        return new SignupResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                patient.getId(),
                null
        );
    }

    public static SignupResponse from(User user, Doctor doctor) {
        return new SignupResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                doctor.getId(),
                doctor.getStatus()
        );
    }
}
