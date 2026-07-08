package com.medflow.user.repository;

import com.medflow.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일 존재 여부
    boolean existsByEmail(String email);

    // 이메일로 회원 조회
    // 회원이 존재하면 Optional<User>, 없으면 Optional.empty() 반환
    Optional<User> findByEmail(String email);
}
