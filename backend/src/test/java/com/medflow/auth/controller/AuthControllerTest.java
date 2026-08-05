package com.medflow.auth.controller;

import com.medflow.auth.dto.response.SignupResponse;
import com.medflow.auth.service.AuthService;
import com.medflow.common.exception.GlobalExceptionHandler;
import com.medflow.user.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    private AuthService authService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void signup_withPatientInformation_returnsSuccess() throws Exception {
        when(authService.signup(any())).thenReturn(
                new SignupResponse(1L, "patient@example.com", UserRole.PATIENT, 10L, null)
        );

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType("application/json")
                        .content("""
                                {
                                  "email": "patient@example.com",
                                  "password": "password123!",
                                  "role": "PATIENT",
                                  "patient": {
                                    "name": "홍길동",
                                    "birth": "1999-05-20",
                                    "gender": "MALE",
                                    "phone": "010-1234-5678"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("patient@example.com"))
                .andExpect(jsonPath("$.data.profileId").value(10L));
    }

    @Test
    void signup_withInvalidNestedPatientInformation_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType("application/json")
                        .content("""
                                {
                                  "email": "patient@example.com",
                                  "password": "password123!",
                                  "role": "PATIENT",
                                  "patient": {
                                    "birth": "1999-05-20",
                                    "gender": "MALE",
                                    "phone": "010-1234-5678"
                                  }
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void signup_withUndefinedRole_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType("application/json")
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "password123!",
                                  "role": "UNKNOWN"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }
}
