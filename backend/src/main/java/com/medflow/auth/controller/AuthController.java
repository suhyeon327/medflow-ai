package com.medflow.auth.controller;

import com.medflow.auth.dto.SignupRequest;
import com.medflow.auth.dto.SignupResponse;
import com.medflow.auth.service.AuthService;
import com.medflow.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ApiResponse<SignupResponse> signup(
            @RequestBody @Valid SignupRequest request
    ) {
        return ApiResponse.success(
                authService.signup(request)
        );
    }

}
