package com.medflow.patient.controller;

import com.medflow.security.config.SecurityConfig;
import com.medflow.security.jwt.JwtTokenParser;
import com.medflow.security.principal.UserPrincipal;
import com.medflow.security.principal.UserPrincipalService;
import com.medflow.common.exception.GlobalExceptionHandler;
import com.medflow.security.handler.CustomAuthenticationEntryPoint;
import com.medflow.patient.dto.PatientResponse;
import com.medflow.patient.entity.Gender;
import com.medflow.patient.service.PatientService;
import com.medflow.user.entity.User;
import com.medflow.user.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientController.class)
@ContextConfiguration(classes = {
        PatientController.class,
        SecurityConfig.class,
        GlobalExceptionHandler.class,
        CustomAuthenticationEntryPoint.class
})
class PatientControllerSecurityTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean PatientService patientService;
    @MockitoBean JwtTokenParser jwtTokenParser;
    @MockitoBean UserPrincipalService userPrincipalService;

    @Test
    void 환자는_본인_프로필을_조회할_수_있다() throws Exception {
        // given
        when(patientService.getPatientProfile(1L)).thenReturn(response());

        // when & then
        mockMvc.perform(get("/api/v1/patients/profile")
                        .with(user(userDetails(1L, UserRole.PATIENT))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10L))
                .andExpect(jsonPath("$.data.name").value("홍길동"));

        verify(patientService).getPatientProfile(1L);
    }

    @Test
    void 환자는_본인_프로필을_수정할_수_있다() throws Exception {
        // given
        when(patientService.updatePatientProfile(eq(1L), any())).thenReturn(response());

        // when & then
        mockMvc.perform(put("/api/v1/patients/profile")
                        .with(user(userDetails(1L, UserRole.PATIENT)))
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "홍길동",
                                  "birth": "1999-05-20",
                                  "gender": "MALE",
                                  "phone": "01012345678"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(patientService).updatePatientProfile(eq(1L), any());
    }

    @Test
    void 환자_프로필_수정_필수값이_null이면_BadRequest를_반환한다() throws Exception {
        // when & then
        mockMvc.perform(put("/api/v1/patients/profile")
                        .with(user(userDetails(1L, UserRole.PATIENT)))
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "홍길동",
                                  "birth": null,
                                  "gender": "MALE",
                                  "phone": "01012345678"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        verify(patientService, never()).updatePatientProfile(any(), any());
    }

    @Test
    void 환자_프로필_수정_이름이_공백이면_BadRequest를_반환한다() throws Exception {
        // when & then
        mockMvc.perform(put("/api/v1/patients/profile")
                        .with(user(userDetails(1L, UserRole.PATIENT)))
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": " ",
                                  "birth": "1999-05-20",
                                  "gender": "MALE",
                                  "phone": "01012345678"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        verify(patientService, never()).updatePatientProfile(any(), any());
    }

    @Test
    void 환자_프로필_수정_전화번호_형식이_잘못되면_BadRequest를_반환한다() throws Exception {
        // when & then
        mockMvc.perform(put("/api/v1/patients/profile")
                        .with(user(userDetails(1L, UserRole.PATIENT)))
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "홍길동",
                                  "birth": "1999-05-20",
                                  "gender": "MALE",
                                  "phone": "010-1234-5678"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        verify(patientService, never()).updatePatientProfile(any(), any());
    }

    @ParameterizedTest
    @EnumSource(value = UserRole.class, names = {"DOCTOR", "ADMIN"})
    void 일반_환자가_아닌_역할은_환자_API에_접근할_수_없다(UserRole role) throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/patients/profile")
                        .with(user(userDetails(2L, role))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("AUTH_006"));
    }

    @Test
    void 인증되지_않은_사용자는_환자_API에_접근할_수_없다() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/patients/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_007"));
    }

    private PatientResponse response() {
        return new PatientResponse(
                10L, "홍길동", LocalDate.of(1999, 5, 20), Gender.MALE, "01012345678"
        );
    }

    private UserPrincipal userDetails(Long userId, UserRole role) {
        User user = User.create(role.name().toLowerCase() + userId + "@test.com", "password", role);
        ReflectionTestUtils.setField(user, "id", userId);
        return UserPrincipal.from(user);
    }
}
