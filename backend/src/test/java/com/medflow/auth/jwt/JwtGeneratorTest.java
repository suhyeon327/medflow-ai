package com.medflow.auth.jwt;

import com.medflow.auth.dto.response.JwtToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.security.Key;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtGeneratorTest {

    private static final String SECRET =
            "bWVkaWxpbmstand0LXNlY3JldC1rZXktZm9yLXRlc3RzLTEyMzQ1Njc4OTA=";

    private final JwtGenerator jwtGenerator = new JwtGenerator(SECRET);

    @Test
    void createToken_issuesAccessAndRefreshTokens() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "patient@example.com",
                        "",
                        List.of(new SimpleGrantedAuthority("ROLE_PATIENT"))
                );

        JwtToken token = jwtGenerator.createToken(authentication);

        assertThat(token.grantType()).isEqualTo("Bearer");
        assertThat(token.accessToken()).isNotBlank();
        assertThat(token.refreshToken()).isNotBlank();
        assertThat(token.accessToken()).isNotEqualTo(token.refreshToken());

        Claims accessClaims = parse(token.accessToken());
        Claims refreshClaims = parse(token.refreshToken());
        assertThat(accessClaims.getSubject()).isEqualTo("patient@example.com");
        assertThat(accessClaims.get("auth", String.class)).isEqualTo("ROLE_PATIENT");
        assertThat(accessClaims.getExpiration()).isAfter(accessClaims.getIssuedAt() == null
                ? new java.util.Date()
                : accessClaims.getIssuedAt());
        assertThat(refreshClaims.getExpiration()).isAfter(accessClaims.getExpiration());
    }

    private Claims parse(String token) {
        Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
