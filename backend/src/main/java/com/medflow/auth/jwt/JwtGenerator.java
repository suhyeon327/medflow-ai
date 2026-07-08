package com.medflow.auth.jwt;

import com.medflow.auth.dto.JwtToken;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

@Component   // Spring Bean으로 관리
public class JwtGenerator {

    private final Key key;

    // application.yml에서 secret 값 가져와서 Key에 저장
    public JwtGenerator(@Value("${jwt.secret}") String secretKey) {   // secretKey는 Base64로 인코딩된 문자열
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);   // 실제 바이트 배열을 사용해 키 생성
        this.key = Keys.hmacShaKeyFor(keyBytes);   // HMAC 알고리즘에 사용할 수 있는 키 생성
    }

    // User 정보를 가지고 AccessToken, RefreshToken을 생성하는 메서드
    public JwtToken createToken(Authentication authentication) {

        // 권한 가져오기
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // 현재 시간
        long now = (new Date()).getTime();

        // Access Token 생성
        Date accessTokenExpiresIn = new Date(now + 3600000);   // 60분 = 3600000
        String accessToken = Jwts.builder()
                .setSubject(authentication.getName())   // Subject 설정, 사용자 식별
                .claim("auth", authorities)
                .setExpiration(accessTokenExpiresIn)   // 만료 시간 설정
                .signWith(key, SignatureAlgorithm.HS256)   // key(비밀키)와 HS256 알고리즘으로 JWT에 서명, 토큰의 위변조 여부 검증
                .compact();   // JWT를 최종 문자열 형태로 생성

        // Refresh Token 생성
        String refreshToken = Jwts.builder()
                .setExpiration(new Date(now + 1209600000))   // 14일 = 1209600000
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return JwtToken.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
