package com.medflow.auth.dto.response;

public record TokenResponse(
        String grantType,   // JWT에 대한 인증 타입
        String accessToken,
        String refreshToken
) {
    public static TokenResponse from(String grantType, String accessToken, String refreshToken) {
        return new TokenResponse(grantType, accessToken, refreshToken);
    }
}
