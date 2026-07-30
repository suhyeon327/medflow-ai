package com.medflow.questionnaire.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medflow.common.exception.GlobalExceptionHandler;
import com.medflow.questionnaire.service.QuestionnaireService;
import com.medflow.questionnaire.service.QuestionnaireAnalysisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class QuestionnaireControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new QuestionnaireController(
                        mock(QuestionnaireService.class),
                        mock(QuestionnaireAnalysisService.class)
                ))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createQuestionnaire_returnsBadRequest_when_requiredValueIsMissing() throws Exception {
        Map<String, Object> request = Map.of(
                "reservationId", 1,
                "symptomStartedAt", "2026-07-29T09:30:00",
                "symptomDescription", "증상 설명"
        );
        mockMvc.perform(post("/api/v1/questionnaires")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createQuestionnaire_returnsBadRequest_when_painLevelIsOutOfRange() throws Exception {
        Map<String, Object> request = Map.of(
                "reservationId", 1,
                "chiefComplaint", "복통",
                "symptomStartedAt", "2026-07-29T09:30:00",
                "symptomDescription", "증상 설명",
                "painLevel", 11
        );
        mockMvc.perform(post("/api/v1/questionnaires")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateQuestionnaire_returnsBadRequest_when_painLevelIsOutOfRange() throws Exception {
        Map<String, Object> request = Map.of(
                "chiefComplaint", "두통",
                "symptomStartedAt", "2026-07-30T08:00:00",
                "symptomDescription", "머리가 아픕니다.",
                "painLevel", -1
        );
        mockMvc.perform(put("/api/v1/questionnaires/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
