package com.medflow.auth.service;

import com.medflow.auth.dto.request.LoginRequest;
import com.medflow.auth.dto.request.LogoutRequest;
import com.medflow.auth.dto.request.ReissueRequest;
import com.medflow.auth.dto.response.TokenResponse;
import com.medflow.auth.token.service.RefreshTokenService;
import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    // 로그인
    public TokenResponse login(LoginRequest request) {

        try {
            // Spring Security에게 인증을 맡김
            Authentication authentication = authenticationManager.authenticate(
                    // 사용자가 입력한 이메일/비밀번호를 인증 요청 객체로 생성
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );

            // 인증에 성공하면 토큰 발급
            return refreshTokenService.issue(authentication);

        } catch (AuthenticationException e) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    // Access Token / Refresh Token 재발급
    public TokenResponse reissue(ReissueRequest request) {
        return refreshTokenService.reissue(request.refreshToken());
    }

    // 로그아웃
    public void logout(Long userId, LogoutRequest request) {
        refreshTokenService.revoke(userId, request.refreshToken());
    }
}
