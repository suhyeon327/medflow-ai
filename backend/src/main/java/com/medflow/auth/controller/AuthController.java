package com.medflow.auth.controller;

import com.medflow.auth.dto.request.LoginRequest;
import com.medflow.auth.dto.request.LogoutRequest;
import com.medflow.auth.dto.request.ReissueRequest;
import com.medflow.auth.dto.response.TokenResponse;
import com.medflow.auth.service.AuthService;
import com.medflow.common.response.ApiResponse;
import com.medflow.security.principal.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authenticationService;

    // 로그인
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(
            @RequestBody @Valid LoginRequest request
    ) {
        return ApiResponse.success(
                authenticationService.login(request)
        );
    }

    // Refresh Token 재발급
    @PostMapping("/reissue")
    public ApiResponse<TokenResponse> reissue(
            @RequestBody @Valid ReissueRequest request
    ) {
        return ApiResponse.success(
                authenticationService.reissue(request)
        );
    }

    // 로그아웃
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody @Valid LogoutRequest request
    ) {
        authenticationService.logout(userPrincipal.getUserId(), request);
        return ApiResponse.success(null);
    }
}
