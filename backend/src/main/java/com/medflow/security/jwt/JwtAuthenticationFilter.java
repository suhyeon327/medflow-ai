package com.medflow.security.jwt;

import com.medflow.common.exception.BusinessException;
import com.medflow.security.principal.UserPrincipal;
import com.medflow.security.principal.UserPrincipalService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenParser jwtTokenParser;
    private final UserPrincipalService userPrincipalService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        // 1. Request Header에서 JWT 토큰 추출
        String token = resolveToken(request);

        // 2. validateToken으로 토큰 유효성 검사
        if (token != null && jwtTokenParser.validateToken(token)) {
            try {
                // 토큰 사용자와 현재 활성 상태를 확인한 뒤 Authentication 객체 생성
                String email = jwtTokenParser.getSubject(token);
                UserPrincipal userPrincipal = userPrincipalService.loadActiveUserByEmail(email);
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userPrincipal,
                        null,
                        userPrincipal.getAuthorities()
                );

                // 토큰이 유효하고 회원이 활성 상태인 경우 SecurityContext에 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (BusinessException e) {
                // 탈퇴, 잠금 또는 존재하지 않는 회원은 인증하지 않음
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    // Request Header에서 토큰 정보 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // 문자열이 null이 아니고, 비어 있지 않으며, 공백만 있는 문자열도 아닌지 확인 && Bearer로 시작하는지 확인
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
