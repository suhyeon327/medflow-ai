package com.medflow.auth.controller;

import com.medflow.auth.jwt.JwtProvider;
import com.medflow.auth.security.CustomUserDetails;
import com.medflow.auth.service.AuthService;
import com.medflow.common.config.SecurityConfig;
import com.medflow.common.exception.GlobalExceptionHandler;
import com.medflow.common.exception.AuthForbiddenException;
import com.medflow.common.security.CustomAuthenticationEntryPoint;
import com.medflow.user.entity.User;
import com.medflow.user.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Stream;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AuthController.class, AuthSecurityTest.RoleProtectedController.class})
@ContextConfiguration(classes = {
        AuthController.class,
        AuthSecurityTest.RoleProtectedController.class,
        SecurityConfig.class,
        GlobalExceptionHandler.class,
        CustomAuthenticationEntryPoint.class
})
class AuthSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @Test
    void protectedApiRejectsUnauthenticatedUser() throws Exception {
        mockMvc.perform(delete("/api/v1/auth/withdraw")
                        .contentType("application/json")
                        .content("{\"password\":\"password123!\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH_007"));
    }

    @Test
    void authenticatedUserCanLogout() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .with(user(userDetails(1L, UserRole.PATIENT)))
                        .contentType("application/json")
                        .content("{\"refreshToken\":\"refresh-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(authService).logout(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.argThat(
                request -> request.refreshToken().equals("refresh-token")
        ));
    }

    @Test
    void logoutWithAnotherUsersRefreshTokenReturnsForbidden() throws Exception {
        doThrow(new AuthForbiddenException()).when(authService).logout(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.any()
        );

        mockMvc.perform(post("/api/v1/auth/logout")
                        .with(user(userDetails(1L, UserRole.PATIENT)))
                        .contentType("application/json")
                        .content("{\"refreshToken\":\"another-users-token\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH_006"));
    }

    @ParameterizedTest
    @MethodSource("allowedRoleRequests")
    void roleProtectedApisAllowMatchingRole(String path, UserRole role) throws Exception {
        mockMvc.perform(get(path).with(user(userDetails(10L, role))))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @MethodSource("forbiddenRoleRequests")
    void roleProtectedApisRejectOtherRoles(String path, UserRole role) throws Exception {
        mockMvc.perform(get(path).with(user(userDetails(10L, role))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("AUTH_006"));
    }

    private static Stream<Arguments> allowedRoleRequests() {
        return Stream.of(
                Arguments.of("/test/security/patient", UserRole.PATIENT),
                Arguments.of("/test/security/doctor", UserRole.DOCTOR),
                Arguments.of("/test/security/admin", UserRole.ADMIN)
        );
    }

    private static Stream<Arguments> forbiddenRoleRequests() {
        return Stream.of(
                Arguments.of("/test/security/patient", UserRole.DOCTOR),
                Arguments.of("/test/security/patient", UserRole.ADMIN),
                Arguments.of("/test/security/doctor", UserRole.PATIENT),
                Arguments.of("/test/security/doctor", UserRole.ADMIN),
                Arguments.of("/test/security/admin", UserRole.PATIENT),
                Arguments.of("/test/security/admin", UserRole.DOCTOR)
        );
    }

    private CustomUserDetails userDetails(Long userId, UserRole role) {
        User user = User.create(role.name().toLowerCase() + userId + "@test.com", "password", role);
        ReflectionTestUtils.setField(user, "id", userId);
        return new CustomUserDetails(user);
    }

    @RestController
    @RequestMapping("/test/security")
    static class RoleProtectedController {

        @GetMapping("/patient")
        @PreAuthorize("hasRole('PATIENT')")
        String patient() {
            return "patient";
        }

        @GetMapping("/doctor")
        @PreAuthorize("hasRole('DOCTOR')")
        String doctor() {
            return "doctor";
        }

        @GetMapping("/admin")
        @PreAuthorize("hasRole('ADMIN')")
        String admin() {
            return "admin";
        }
    }
}
