package com.medflow.common.init;

import com.medflow.user.entity.User;
import com.medflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "admin.bootstrap-enabled",
        havingValue = "true"
)
public class AdminInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Bean
    CommandLineRunner initAdmin() {
        return args -> {
            if (userRepository.existsByEmail(adminEmail)) {
                return;
            }

            User admin = User.createAdmin(
                    adminEmail,
                    passwordEncoder.encode(adminPassword)
            );

            userRepository.save(admin);
        };
    }
}