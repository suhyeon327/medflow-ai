package com.medflow.auth.service;

import com.medflow.auth.dto.SignupRequest;
import com.medflow.auth.dto.SignupResponse;
import com.medflow.user.entity.User;
import com.medflow.user.entity.UserRole;
import com.medflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SignupResponse signup(SignupRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        if (request.getRole() == UserRole.ADMIN) {
            throw new IllegalArgumentException("ADMIN 계정은 일반 회원가입으로 생성할 수 없습니다.");
        }

        User user = User.create(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getRole()
        );

        User saveUser = userRepository.save(user);

        return SignupResponse.builder()
                .id(saveUser.getId())
                .email(saveUser.getEmail())
                .role(saveUser.getRole())
                .build();
    }
}
