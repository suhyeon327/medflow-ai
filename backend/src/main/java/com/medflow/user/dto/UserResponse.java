package com.medflow.user.dto;

import com.medflow.user.entity.UserRole;
import com.medflow.user.entity.UserStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private UserRole role;
}
