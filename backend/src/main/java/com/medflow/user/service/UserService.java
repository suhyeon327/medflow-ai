package com.medflow.user.service;

import com.medflow.common.exception.UserNotFoundException;
import com.medflow.user.dto.AdminUserResponse;
import com.medflow.user.entity.User;
import com.medflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // ID로 단일 회원 조회
    public AdminUserResponse getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);

        return AdminUserResponse.from(user);
    }

    // 전체 회원 조회
    public List<AdminUserResponse> getUsers() {

        return userRepository.findAll()
                .stream()
                .map(AdminUserResponse::from)
                .toList();
    }
}
