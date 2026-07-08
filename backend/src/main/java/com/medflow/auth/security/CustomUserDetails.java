package com.medflow.auth.security;

import com.medflow.user.entity.User;
import com.medflow.user.entity.UserRole;
import com.medflow.user.entity.UserStatus;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    // 실제 회원 엔티티
    private final User user;

    // 생성자, 로그인한 회원 정보
    public CustomUserDetails(User user) {
        this.user = user;
    }

    // 사용자의 권한(role) 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
    }

    // Spring Security에서 사용하는 비밀번호
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    // Spring Security에서 username으로 사용하는 값(email)
    public String getUsername() {
        return user.getEmail();
    }

    // 계정 만료 여부 (true: 만료되지 않음, false: 만료됨)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정 잠금 여부 (true: LOCKED 상태가 아닌 경우)
    @Override
    public boolean isAccountNonLocked() {
        return user.getStatus() != UserStatus.LOCKED;
    }

    // 비밀번호 만료 여부 (비밀번호 만료 기능을 사용하지 않으므로 항상 true)
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정 활성화 여부 (ACTIVE 상태인 경우만 로그인 가능)
    @Override
    public boolean isEnabled() {
        return user.getStatus() == UserStatus.ACTIVE;
    }
}
