package com.medflow.auth.jwt;

import com.medflow.common.exception.TokenMissingAuthorityException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

    private final Key key;

    // application.yml에서 Base64로 인코딩된 secret 값을 읽어 서명용 Key 생성
    public JwtProvider(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // JWT를 복호화해서 Spring Security가 사용할 Authentication 객체 생성
    public Authentication getAuthentication(String accessToken) {

        // JWT Payload(Claims) 추출
        Claims claims = parseClaims(accessToken);

        // 권한 정보가 없는 토큰은 인증 객체를 만들 수 없으므로 예외 처리
        if (claims.get("auth") == null) {
            throw new TokenMissingAuthorityException();
        }

        // Claims에 저장된 권한 문자열을 GrantedAuthority 목록으로 변환
        Collection<? extends GrantedAuthority> authorities = Arrays.stream(claims.get("auth").toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .toList();

        // Spring Security에서 사용할 UserDetails 생성
        UserDetails principal = new User(claims.getSubject(), "", authorities);

        // 인증 완료 상태의 Authentication 반환
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    // JWT 서명, 형식, 만료 여부를 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            // 서명이 올바르지 않거나 JWT 형식이 잘못된 경우
            log.info("Invalid JWT Token", e);
        } catch (ExpiredJwtException e) {
            // 토큰이 만료된 경우
            log.info("Expired JWT Token");
        } catch (UnsupportedJwtException e) {
            // 지원하지 않는 JWT 형식인 경우
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            // 토큰 문자열이 비어 있거나 null에 가까운 값인 경우
            log.info("JWT claims string is empty");
        }
        return false;
    }

    // Refresh Token 만료 시간을 DB에 저장하기 위해 JWT의 expiration 값 추출
    public Date getExpiration(String token) {
        return parseClaims(token).getExpiration();
    }

    // JWT를 파싱해서 Payload(Claims)를 꺼내는 메서드
    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // 만료된 Access Token에서도 Claims를 꺼내야 재발급 흐름에 사용할 수 있음
            return e.getClaims();
        }
    }
}
