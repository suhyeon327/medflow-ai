package com.medflow.user.dto;

import com.medflow.patient.dto.AdminPatientResponse;
import com.medflow.patient.entity.Patient;
import com.medflow.user.entity.User;
import com.medflow.user.entity.UserRole;
import com.medflow.user.entity.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminUserResponse {
    private Long userId;
    private String email;
    private UserRole role;
    private UserStatus status;
//    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public static AdminUserResponse from(User user) {
        return AdminUserResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
//                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .deletedAt(user.getDeletedAt())
                .build();
    }
}
