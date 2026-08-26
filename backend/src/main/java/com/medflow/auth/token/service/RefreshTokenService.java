package com.medflow.auth.token.service;

import com.medflow.auth.dto.response.TokenResponse;
import com.medflow.auth.token.entity.RefreshToken;
import com.medflow.auth.token.repository.RefreshTokenRepository;
import com.medflow.common.exception.AuthForbiddenException;
import com.medflow.common.exception.InvalidCredentialsException;
import com.medflow.common.exception.UserNotFoundException;
import com.medflow.security.jwt.JwtTokenGenerator;
import com.medflow.security.jwt.JwtTokenParser;
import com.medflow.security.principal.UserPrincipal;
import com.medflow.user.entity.User;
import com.medflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenGenerator jwtTokenGenerator;
    private final JwtTokenParser jwtTokenParser;

    // 인증 완료된 Authentication으로 JWT를 생성하고 Refresh Token 저장
    public TokenResponse issue(Authentication authentication) {
        TokenResponse tokenResponse = jwtTokenGenerator.createToken(authentication);

        // Refresh Token을 DB에 저장하거나 기존 토큰을 갱신
        saveOrRenewRefreshToken(authentication.getName(), tokenResponse.refreshToken());
        return tokenResponse;
    }

    // Access Token / Refresh Token 재발급
    public TokenResponse reissue(String refreshTokenValue) {

        // 요청으로 받은 Refresh Token이 DB에 저장되어 있는지 확인
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(InvalidCredentialsException::new);

        // JWT 자체가 유효하지 않거나 DB 기준 만료 시간이 지났으면 삭제 후 실패 처리
        if (!jwtTokenParser.validateToken(refreshTokenValue) || refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidCredentialsException();
        }

        // Refresh Token과 연결된 사용자 조회
        User user = refreshToken.getUser();
        if (!user.isActive()) {
            throw new InvalidCredentialsException();
        }

        // 토큰 재발급을 위해 User 엔티티를 Spring Security의 Authentication으로 변환
        UserPrincipal userPrincipal = UserPrincipal.from(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal,
                "",
                userPrincipal.getAuthorities()
        );

        // 새로운 Access Token과 Refresh Token 발급
        TokenResponse tokenResponse = jwtTokenGenerator.createToken(authentication);

        // DB에 저장된 Refresh Token도 새 값과 새 만료 시간으로 갱신
        refreshToken.renew(
                tokenResponse.refreshToken(),
                toLocalDateTime(jwtTokenParser.getExpiration(tokenResponse.refreshToken()))
        );

        return tokenResponse;
    }

    // 로그아웃 시 서버에 저장된 Refresh Token을 삭제해서 이후 재발급을 막음
    public void revoke(Long userId, String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(refreshToken -> {
                    if (!userId.equals(refreshToken.getUser().getId())) {
                        throw new AuthForbiddenException();
                    }
                    refreshTokenRepository.delete(refreshToken);
                });
    }

    // 회원 탈퇴 시 사용자의 모든 Refresh Token 삭제
    public void revokeAll(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    // Refresh Token 저장 또는 갱신
    private void saveOrRenewRefreshToken(String email, String token) {

        // 인증된 사용자의 이메일로 User 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        // JWT에 들어있는 만료 시간을 DB 저장 형식으로 변환
        LocalDateTime expiresAt = toLocalDateTime(jwtTokenParser.getExpiration(token));

        // 사용자별 Refresh Token이 이미 있으면 갱신, 없으면 새로 저장
        refreshTokenRepository.findByUser(user)
                .ifPresentOrElse(
                        refreshToken -> refreshToken.renew(token, expiresAt),
                        () -> refreshTokenRepository.save(RefreshToken.create(user, token, expiresAt))
                );
    }

    // java.util.Date를 LocalDateTime으로 변환
    private LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}
