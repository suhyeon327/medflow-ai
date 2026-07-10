package com.medflow.token.repository;

import com.medflow.token.entity.RefreshToken;
import com.medflow.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // 토큰 정보 조회
    Optional<RefreshToken> findByToken(String token);

    // 사용자에 해당하는 Refresh Token 조회
    Optional<RefreshToken> findByUser(User user);

    // 로그아웃 시 해당 사용자의 Refresh Token 삭제
    void deleteByUserId(Long userId);
}
