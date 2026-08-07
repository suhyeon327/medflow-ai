package com.medflow.user.controller;

import com.medflow.auth.jwt.JwtProvider;
import com.medflow.auth.security.CustomUserDetails;
import com.medflow.common.config.SecurityConfig;
import com.medflow.common.exception.GlobalExceptionHandler;
import com.medflow.common.security.CustomAuthenticationEntryPoint;
import com.medflow.user.dto.AdminUserPageResponse;
import com.medflow.user.dto.AdminUserResponse;
import com.medflow.user.entity.User;
import com.medflow.user.entity.UserRole;
import com.medflow.user.entity.UserStatus;
import com.medflow.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserController.class)
@ContextConfiguration(classes = {
        AdminUserController.class,
        SecurityConfig.class,
        GlobalExceptionHandler.class,
        CustomAuthenticationEntryPoint.class
})
class AdminUserControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean UserService userService;
    @MockitoBean JwtProvider jwtProvider;

    @Test
    void 관리자는_사용자_단건을_조회할_수_있다() throws Exception {
        // given
        when(userService.getUser(10L)).thenReturn(userResponse());

        // when & then
        mockMvc.perform(get("/api/v1/admin/users/{userId}", 10L)
                        .with(user(userDetails(1L, UserRole.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(10L))
                .andExpect(jsonPath("$.data.email").value("patient@test.com"));

        verify(userService).getUser(10L);
    }

    @Test
    void 관리자는_역할과_상태로_사용자를_페이징_조회할_수_있다() throws Exception {
        // given
        when(userService.getUsers(eq(UserRole.PATIENT), eq(UserStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(new AdminUserPageResponse(List.of(userResponse()), 1, 5, 6, 2));

        // when & then
        mockMvc.perform(get("/api/v1/admin/users")
                        .with(user(userDetails(1L, UserRole.ADMIN)))
                        .param("role", "PATIENT")
                        .param("status", "ACTIVE")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(5))
                .andExpect(jsonPath("$.data.totalElements").value(6));

        verify(userService).getUsers(
                eq(UserRole.PATIENT),
                eq(UserStatus.ACTIVE),
                org.mockito.ArgumentMatchers.argThat(pageable ->
                        pageable.getPageNumber() == 1 && pageable.getPageSize() == 5)
        );
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class, names = {"PATIENT", "DOCTOR"})
    void 관리자가_아닌_사용자는_관리자_사용자_API에_접근할_수_없다(UserRole role) throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/admin/users")
                        .with(user(userDetails(2L, role))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("AUTH_006"));
    }

    @Test
    void 인증되지_않은_사용자는_관리자_사용자_API에_접근할_수_없다() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_007"));
    }

    @Test
    void 지원하지_않는_역할_필터는_BadRequest를_반환한다() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/admin/users")
                        .with(user(userDetails(1L, UserRole.ADMIN)))
                        .param("role", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void 숫자가_아닌_사용자_ID는_BadRequest를_반환한다() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/admin/users/not-number")
                        .with(user(userDetails(1L, UserRole.ADMIN))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    private AdminUserResponse userResponse() {
        return new AdminUserResponse(
                10L, "patient@test.com", UserRole.PATIENT, UserStatus.ACTIVE,
                null, null, null
        );
    }

    private CustomUserDetails userDetails(Long userId, UserRole role) {
        User user = User.create(role.name().toLowerCase() + userId + "@test.com", "password", role);
        ReflectionTestUtils.setField(user, "id", userId);
        return new CustomUserDetails(user);
    }
}
