package com.medflow.auth.service;

import com.medflow.auth.dto.SignupRequest;
import com.medflow.auth.dto.SignupResponse;
import com.medflow.user.repository.UserRepository;
import com.medflow.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SignupResponse signup(SignupRequest request) {

        // 이메일 중복 체크
        if (userRepository.existByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // User 생성
        User user = User.create(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getRole()
        );

        // 저장
        User saveUser = userRepository.save(user);

        return SignupResponse.builder()
                .id(saveUser.getId())
                .email(saveUser.getEmail())
                .role(saveUser.getRole())
                .build();
    }
}
