package com.medflow.auth.service;

import com.medflow.auth.dto.JwtToken;
import com.medflow.auth.dto.LoginRequest;
import com.medflow.auth.dto.LogoutRequest;
import com.medflow.auth.dto.ReissueRequest;
import com.medflow.auth.dto.SignupRequest;
import com.medflow.auth.dto.SignupResponse;
import com.medflow.auth.jwt.JwtGenerator;
import com.medflow.auth.jwt.JwtProvider;
import com.medflow.auth.security.CustomUserDetails;
import com.medflow.common.exception.EmailAlreadyExistsException;
import com.medflow.common.exception.InvalidCredentialsException;
import com.medflow.common.exception.UserNotFoundException;
import com.medflow.token.entity.RefreshToken;
import com.medflow.token.repository.RefreshTokenRepository;
import com.medflow.user.entity.User;
import com.medflow.user.entity.UserRole;
import com.medflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtGenerator jwtGenerator;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;

    // 회원가입
    public SignupResponse signup(SignupRequest request) {

        // 이메일 중복 여부
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException();
        }

        // User 생성
        // 일반 사용자가 요청으로 ADMIN/DOCTOR 권한을 받을 수 없도록 기본 권한을 PATIENT로 고정
        User user = User.create(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                UserRole.PATIENT
        );

        // 데이터베이스 저장
        User savedUser = userRepository.save(user);

        // 응답
        return SignupResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .build();
    }

    // 로그인
    public JwtToken login(LoginRequest request) {

        try {
            // AuthenticationManager가 UserDetailsService와 PasswordEncoder를 사용해 인증 처리
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // 인증 완료된 Authentication으로 JWT 생성
            JwtToken jwtToken = jwtGenerator.createToken(authentication);

            // Refresh Token을 DB에 저장하거나 기존 토큰을 갱신
            saveOrRenewRefreshToken(authentication.getName(), jwtToken.refreshToken());
            return jwtToken;
        } catch (BadCredentialsException e) {
            // 이메일 또는 비밀번호가 틀린 경우 401 응답으로 변환
            throw new InvalidCredentialsException();
        }
    }

    // Access Token / Refresh Token 재발급
    public JwtToken reissue(ReissueRequest request) {

        String refreshTokenValue = request.getRefreshToken();

        // 요청으로 받은 Refresh Token이 DB에 저장되어 있는지 확인
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(InvalidCredentialsException::new);

        // JWT 자체가 유효하지 않거나 DB 기준 만료 시간이 지났으면 삭제 후 실패 처리
        if (!jwtProvider.validateToken(refreshTokenValue) || refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidCredentialsException();
        }

        // Refresh Token과 연결된 사용자 조회
        User user = refreshToken.getUser();
        if (!user.isActive()) {
            throw new InvalidCredentialsException();
        }

        // 토큰 재발급을 위해 User 엔티티를 Spring Security의 Authentication으로 변환
        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                "",
                userDetails.getAuthorities()
        );

        // 새로운 Access Token과 Refresh Token 발급
        JwtToken jwtToken = jwtGenerator.createToken(authentication);

        // DB에 저장된 Refresh Token도 새 값과 새 만료 시간으로 갱신
        refreshToken.renew(
                jwtToken.refreshToken(),
                toLocalDateTime(jwtProvider.getExpiration(jwtToken.refreshToken()))
        );

        return jwtToken;
    }

    // 로그아웃
    public void logout(LogoutRequest request) {

        // 서버에 저장된 Refresh Token을 삭제해서 이후 재발급을 막음
        refreshTokenRepository.findByToken(request.getRefreshToken())
                .ifPresent(refreshTokenRepository::delete);
    }

    // Refresh Token 저장 또는 갱신
    private void saveOrRenewRefreshToken(String email, String token) {

        // 인증된 사용자의 이메일로 User 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        // JWT에 들어있는 만료 시간을 DB 저장 형식으로 변환
        LocalDateTime expiresAt = toLocalDateTime(jwtProvider.getExpiration(token));

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
