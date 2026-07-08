package com.medflow.common.config;

import com.medflow.auth.jwt.JwtAuthenticationFilter;
import com.medflow.auth.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration   // 설정 클래스
@RequiredArgsConstructor
@EnableWebSecurity   // 시큐리티 활성화
public class SecurityConfig {

    private final JwtProvider jwtProvider;

    // 보안 정책 정의
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())   // CSRF 비활성화
                .formLogin(form -> form.disable())   // 기본 로그인 페이지 비활성화
                .httpBasic(basic -> basic.disable())   // HTTP Basic 인증 비활성화
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))   // JWT를 사용하기 때문에 세션을 사용하지 않음
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/api/v1/auth/signup",
                                "/api/v1/auth/login",
                                "/api/v1/auth/reissue",
                                "/api/v1/auth/logout",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()   // 누구나 접속 가능한 주소
                        .anyRequest().authenticated()   // 인증(로그인) 해야 접속 가능
                )

                // UsernamePasswordAuthenticationFilter 보다 JWT 검사를 먼저 실행
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtProvider),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    // Spring Security 인증을 위한 AuthenticationManager 등록
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // 비밀번호 암호화
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
