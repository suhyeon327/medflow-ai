package com.medflow.auth.controller;

import com.medflow.auth.dto.request.*;
import com.medflow.auth.dto.response.JwtToken;
import com.medflow.auth.dto.response.SignupResponse;
import com.medflow.auth.dto.response.WithdrawResponse;
import com.medflow.auth.security.CustomUserDetails;
import com.medflow.auth.service.AuthService;
import com.medflow.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid LogoutRequest request
    ) {
        authService.logout(userDetails.getUserId(), request);
        return ApiResponse.success(null);
    }

    // 회원 탈퇴
    @DeleteMapping("/withdraw")
    public ApiResponse<WithdrawResponse> withdraw(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid WithdrawRequest request
    ) {
        return ApiResponse.success(
                authService.withdraw(userDetails.getUserId(), request)
        );
    }
}
