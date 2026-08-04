package com.medflow.common.config;

import com.medflow.auth.jwt.JwtAuthenticationFilter;
import com.medflow.auth.jwt.JwtProvider;
import com.medflow.common.security.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration   // 설정 클래스
@RequiredArgsConstructor
@EnableWebSecurity   // 시큐리티 활성화
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    // 보안 정책 정의
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())   // CSRF 비활성화
                .formLogin(form -> form.disable())   // 기본 로그인 페이지 비활성화
                .httpBasic(basic -> basic.disable())   // HTTP Basic 인증 비활성화
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))   // JWT를 사용하기 때문에 세션을 사용하지 않음
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/",
                                "/api/v1/auth/signup",
                                "/api/v1/auth/login",
                                "/api/v1/auth/reissue",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()   // 누구나 접속 가능한 주소
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/hospitals",
                                "/api/v1/hospitals/{hospitalId}",
                                "/api/v1/hospitals/{hospitalId}/doctors",
                                "/api/v1/doctors/{doctorId}"
                        ).permitAll()
                        .anyRequest().authenticated()   // 인증(로그인) 해야 접속 가능
                )

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(   // 인증 실패 401
                                authenticationEntryPoint
                        )
                )

                // UsernamePasswordAuthenticationFilter 보다 JWT 검사를 먼저 실행
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtProvider),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    // 프론트엔드에서 백엔드 API에 접근할 수 있도록 CORS 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // React 개발 서버 주소
        configuration.setAllowedOrigins(
                List.of("http://localhost:5173")
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of("*")
        );

        // 프론트엔드에서 Authorization 응답 헤더를 읽을 수 있도록 허용
        configuration.setExposedHeaders(
                List.of("Authorization")
        );

        // 쿠키와 인증 정보 포함 요청 허용
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
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
