package com.medflow.auth.dto;

import lombok.Builder;

@Builder
public record JwtToken(
        String grantType,   // JWT에 대한 인증 타입
        String accessToken,
        String refreshToken
) {
}
