package com.medflow.registration.controller;

import com.medflow.common.response.ApiResponse;
import com.medflow.registration.dto.request.SignupRequest;
import com.medflow.registration.dto.response.SignupResponse;
import com.medflow.registration.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    // 회원가입
    @PostMapping("/signup")
    public ApiResponse<SignupResponse> signup(
            @RequestBody @Valid SignupRequest request
    ) {
        return ApiResponse.success(
                registrationService.signup(request)
        );
    }
}
