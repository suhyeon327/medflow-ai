package com.medflow.questionnaire.controller;

import com.medflow.auth.jwt.JwtProvider;
import com.medflow.auth.security.CustomUserDetails;
import com.medflow.common.config.SecurityConfig;
import com.medflow.common.exception.GlobalExceptionHandler;
import com.medflow.common.security.CustomAuthenticationEntryPoint;
import com.medflow.questionnaire.dto.response.DoctorQuestionnaireAnalysisResponse;
import com.medflow.questionnaire.dto.response.QuestionnaireAnalysisDetailResponse;
import com.medflow.questionnaire.entity.PriorityLevel;
import com.medflow.questionnaire.entity.QuestionnaireAnalysisStatus;
import com.medflow.questionnaire.service.DoctorQuestionnairesService;
import com.medflow.questionnaire.service.QuestionnaireAnalysisService;
import com.medflow.questionnaire.service.QuestionnaireService;
import com.medflow.user.entity.User;
import com.medflow.user.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({QuestionnaireController.class, DoctorQuestionnairesController.class})
@ContextConfiguration(classes = {
        QuestionnaireController.class,
        DoctorQuestionnairesController.class,
        SecurityConfig.class,
        GlobalExceptionHandler.class,
        CustomAuthenticationEntryPoint.class
})
class QuestionnaireControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuestionnaireService questionnaireService;

    @MockitoBean
    private QuestionnaireAnalysisService questionnaireAnalysisService;

    @MockitoBean
    private DoctorQuestionnairesService doctorQuestionnairesService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @Test
    void patientCanAccessOwnQuestionnaireAnalysisApi() throws Exception {
        when(questionnaireAnalysisService.getAnalysis(1L, 20L))
                .thenReturn(new QuestionnaireAnalysisDetailResponse(
                        30L, 20L, "요약", List.of(), List.of(), List.of(),
                        PriorityLevel.NORMAL, QuestionnaireAnalysisStatus.COMPLETED
                ));

        mockMvc.perform(get("/api/v1/questionnaires/{questionnaireId}/analysis", 20L)
                        .with(user(userDetails(1L, UserRole.PATIENT))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.questionnaireId").value(20L));

        verify(questionnaireAnalysisService).getAnalysis(1L, 20L);
    }

    @Test
    void doctorCanAccessAssignedQuestionnaireAnalysisApi() throws Exception {
        when(doctorQuestionnairesService.getQuestionnaireAnalysis(2L, 20L))
                .thenReturn(new DoctorQuestionnaireAnalysisResponse(
                        30L, 20L, 10L, "요약", List.of(), List.of(), List.of(),
                        PriorityLevel.NORMAL, QuestionnaireAnalysisStatus.COMPLETED
                ));

        mockMvc.perform(get("/api/v1/doctors/questionnaires/{questionnaireId}/analysis", 20L)
                        .with(user(userDetails(2L, UserRole.DOCTOR))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.questionnaireId").value(20L));

        verify(doctorQuestionnairesService).getQuestionnaireAnalysis(2L, 20L);
    }

    @ParameterizedTest
    @MethodSource("forbiddenRoleRequests")
    void questionnaireApisRejectOtherRoles(String path, UserRole role) throws Exception {
        mockMvc.perform(get(path).with(user(userDetails(10L, role))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @ParameterizedTest
    @MethodSource("questionnaireApiPaths")
    void questionnaireApisRequireAuthentication(String path) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    private static Stream<Arguments> forbiddenRoleRequests() {
        return Stream.of(
                Arguments.of("/api/v1/questionnaires/20/analysis", UserRole.DOCTOR),
                Arguments.of("/api/v1/questionnaires/20/analysis", UserRole.ADMIN),
                Arguments.of("/api/v1/doctors/questionnaires/20/analysis", UserRole.PATIENT),
                Arguments.of("/api/v1/doctors/questionnaires/20/analysis", UserRole.ADMIN)
        );
    }

    private static Stream<String> questionnaireApiPaths() {
        return Stream.of(
                "/api/v1/questionnaires/10/questionnaire",
                "/api/v1/questionnaires/20/analysis",
                "/api/v1/doctors/questionnaires/20/analysis"
        );
    }

    private CustomUserDetails userDetails(Long userId, UserRole role) {
        User user = User.create(role.name().toLowerCase() + userId + "@test.com", "password", role);
        ReflectionTestUtils.setField(user, "id", userId);
        return new CustomUserDetails(user);
    }
}
