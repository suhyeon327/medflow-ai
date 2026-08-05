package com.medflow.auth.dto.response;

public record JwtToken(
        String grantType,   // JWT에 대한 인증 타입
        String accessToken,
        String refreshToken
) {
    public static JwtToken from(String grantType, String accessToken, String refreshToken) {
        return new JwtToken(grantType, accessToken, refreshToken);
    }
}
