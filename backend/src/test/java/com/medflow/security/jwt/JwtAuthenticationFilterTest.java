package com.medflow.security.jwt;

import com.medflow.common.exception.AuthUnauthorizedException;
import com.medflow.security.principal.UserPrincipalService;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenParser jwtTokenParser;

    @Mock
    private UserPrincipalService userPrincipalService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void withdrawnUserWithValidAccessToken_isNotAuthenticated() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-access-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        when(jwtTokenParser.validateToken("valid-access-token")).thenReturn(true);
        when(jwtTokenParser.getSubject("valid-access-token")).thenReturn("withdrawn@example.com");
        when(userPrincipalService.loadActiveUserByEmail("withdrawn@example.com"))
                .thenThrow(new AuthUnauthorizedException());

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                jwtTokenParser,
                userPrincipalService
        );

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(filterChain.getRequest()).isSameAs(request);
    }
}
