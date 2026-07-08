package com.medflow.auth.service;

import com.medflow.auth.security.CustomUserDetails;
import com.medflow.common.exception.UserNotFoundException;
import com.medflow.user.entity.User;
import com.medflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    // 회원 조회 Repository
    private final UserRepository userRepository;

    // 이메일로 회원 조회
    @Override
    public UserDetails loadUserByUsername(String email)
        throws UserNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        // User 엔티티를 Security에서 사용할 UserDetails로 변환
        return new CustomUserDetails(user);
    }
}
