package com.medflow.user.service;

import com.medflow.common.exception.UserNotFoundException;
import com.medflow.user.dto.AdminUserResponse;
import com.medflow.user.dto.AdminUserPageResponse;
import com.medflow.user.entity.User;
import com.medflow.user.entity.UserRole;
import com.medflow.user.entity.UserStatus;
import com.medflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    // ID로 단일 회원 조회
    public AdminUserResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        return AdminUserResponse.from(user);
    }

    // 전체 회원 조회 및 필터링
    public AdminUserPageResponse getUsers(
            UserRole role,
            UserStatus status,
            Pageable pageable
    ) {
        Page<User> users;

        if (role != null && status != null) {
            users = userRepository.findAllByRoleAndStatus(role, status, pageable);
        } else if (role != null) {
            users = userRepository.findAllByRole(role, pageable);
        } else if (status != null) {
            users = userRepository.findAllByStatus(status, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        return AdminUserPageResponse.from(users.map(AdminUserResponse::from));
    }
}
