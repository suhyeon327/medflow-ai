package com.medflow.user.entity;

import com.medflow.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)   // 생성자를 통해서 값 변경 목적으로 접근하는 메시지들 차단
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // 기본 키 생성 전략을 설정 - 데이터베이스가 자동으로 값을 생성
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "role", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    private LocalDateTime lastLoginAt;

    // 회원 생성
    public static User create(
            String email,
            String password,
            UserRole role
    ) {
        User user = new User();
        user.email = email;
        user.password = password;
        user.role = role;
        user.status = UserStatus.ACTIVE;
        return user;
    }

    // 비밀번호 번경
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    // 로그인 시간 갱신
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    // 회원 잠금
    public void lock() {
        this.status = UserStatus.LOCKED;
    }

    // 회원 탈퇴
    public void withdraw() {
        this.status = UserStatus.WITHDRAWN;
        softDelete();
    }

    // 회원 활성화 확인
    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }
}
