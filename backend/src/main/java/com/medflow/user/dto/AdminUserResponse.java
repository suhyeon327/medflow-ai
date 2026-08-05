package com.medflow.user.dto;

import com.medflow.user.entity.User;
import com.medflow.user.entity.UserRole;
import com.medflow.user.entity.UserStatus;

import java.time.LocalDateTime;

public record AdminUserResponse(
        Long userId,
        String email,
        UserRole role,
        UserStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
) {

    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getDeletedAt()
        );
    }
}
