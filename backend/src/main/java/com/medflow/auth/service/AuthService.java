package com.medflow.auth.service;

import com.medflow.auth.dto.SignupRequest;
import com.medflow.auth.dto.SignupResponse;
import com.medflow.common.exception.EmailAlreadyExistsException;
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

        // 이메일 중복 여부
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException();
        }

        // User 생성
        User user = User.create(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getRole()
        );

        // 데이터베이스 저장
        User saveUser = userRepository.save(user);

        // 출력
        return SignupResponse.builder()
                .id(saveUser.getId())
                .email(saveUser.getEmail())
                .role(saveUser.getRole())
                .build();
    }
}
