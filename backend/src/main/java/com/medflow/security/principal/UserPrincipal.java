package com.medflow.security.principal;

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
public class UserPrincipal implements UserDetails {

    private final Long userId;
    private final String email;
    private final String password;
    private final UserRole role;
    private final UserStatus status;

    private UserPrincipal(
            Long userId,
            String email,
            String password,
            UserRole role,
            UserStatus status
    ) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = status;
    }

    public static UserPrincipal from(User user) {
        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getRole(),
                user.getStatus()
        );
    }

    // 사용자의 권한(role) 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority("ROLE_" + role.name())
        );
    }

    // Spring Security에서 사용하는 비밀번호
    @Override
    public String getPassword() {
        return password;
    }

    // Spring Security에서 username으로 사용하는 값(email)
    @Override
    public String getUsername() {
        return email;
    }

    // 활성 회원만 로그인 인증 허용
    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }
}
