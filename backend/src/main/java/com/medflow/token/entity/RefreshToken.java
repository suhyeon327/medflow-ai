package com.medflow.token.entity;

import com.medflow.common.entity.BaseEntity;
import com.medflow.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "refresh_token",
    uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_refresh_token_token",
                    columnNames = "token"
            )
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)   // LAZY: User는 필요할 때 추가 조회, optional = false: NULL 허용 안함
    @JoinColumn(name = "user_id", nullable = false)   // JoinColumn: FK 생성
    private User user;

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    private RefreshToken(
            User user,
            String token,
            LocalDateTime expiresAt
    ) {
        this.user = user;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    // Refresh Token DB에 저장
    public static RefreshToken create(
            User user,
            String token,
            LocalDateTime expiresAt
    ) {
        return new RefreshToken(
                user,
                token,
                expiresAt
        );
    }

    // Refresh Token 만료 여부
    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());   // true: 만료, false: 만료 전
    }

    // Refresh Token 갱신 (새 로그인할 경우)
    public void renew(
            String token,
            LocalDateTime expiresAt
    ) {
        this.token = token;
        this.expiresAt = expiresAt;
    }
}
