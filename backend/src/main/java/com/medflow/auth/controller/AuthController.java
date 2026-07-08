package com.medflow.auth.controller;

import com.medflow.auth.dto.JwtToken;
import com.medflow.auth.dto.LoginRequest;
import com.medflow.auth.dto.LogoutRequest;
import com.medflow.auth.dto.ReissueRequest;
import com.medflow.auth.dto.SignupRequest;
import com.medflow.auth.dto.SignupResponse;
import com.medflow.auth.service.AuthService;
import com.medflow.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 회원가입
    @PostMapping("/signup")
    public ApiResponse<SignupResponse> signup(
            @RequestBody @Valid SignupRequest request
    ) {
        return ApiResponse.success(
                authService.signup(request)
        );
    }

    // 로그인
    @PostMapping("/login")
    public ApiResponse<JwtToken> login(
            @RequestBody @Valid LoginRequest request
    ) {
        return ApiResponse.success(
                authService.login(request)
        );
    }

    // Refresh Token 재발급
    @PostMapping("/reissue")
    public ApiResponse<JwtToken> reissue(
            @RequestBody @Valid ReissueRequest request
    ) {
        return ApiResponse.success(
                authService.reissue(request)
        );
    }

    // 로그아웃
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestBody @Valid LogoutRequest request
    ) {
        authService.logout(request);
        return ApiResponse.success(null);
    }
}
