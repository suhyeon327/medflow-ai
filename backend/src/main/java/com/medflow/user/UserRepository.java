package com.medflow.user;

import com.medflow.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // email로 회원을 조회한다.
    // 회원이 존재하면 Optional<User>, 없으면 Optional.empty()를 반환한다.
    Optional<User> findByEmail(String email);
}
