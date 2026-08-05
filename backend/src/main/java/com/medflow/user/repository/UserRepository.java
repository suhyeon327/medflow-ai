package com.medflow.user.repository;

import com.medflow.user.entity.User;
import com.medflow.user.entity.UserRole;
import com.medflow.user.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일 존재 여부
    boolean existsByEmail(String email);

    // 이메일로 회원 조회
    // 회원이 존재하면 Optional<User>, 없으면 Optional.empty() 반환
    Optional<User> findByEmail(String email);

    // ID로 회원 조회
    Optional<User> findById(Long id);

    Page<User> findAllByRole(UserRole role, Pageable pageable);

    Page<User> findAllByStatus(UserStatus status, Pageable pageable);

    Page<User> findAllByRoleAndStatus(UserRole role, UserStatus status, Pageable pageable);
}
