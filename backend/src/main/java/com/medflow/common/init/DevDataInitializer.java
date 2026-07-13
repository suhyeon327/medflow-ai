package com.medflow.common.init;

import com.medflow.user.entity.User;
import com.medflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DevDataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initAdmin() {

        return args -> {

            String email = "admin@example.com";

            if (userRepository.existsByEmail(email)) {
                return;
            }

            User admin = User.createAdmin(
                    email,
                    passwordEncoder.encode("admin1234")
            );

            userRepository.save(admin);
        };
    }
}
