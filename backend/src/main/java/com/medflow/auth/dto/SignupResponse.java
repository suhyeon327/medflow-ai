package com.medflow.auth.dto;

import com.medflow.user.entity.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupResponse {

    private Long id;
    private String email;
    private UserRole role;
}
