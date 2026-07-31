package com.medflow.questionnaire.controller;

import com.medflow.auth.security.CustomUserDetails;
import com.medflow.common.response.ApiResponse;
import com.medflow.questionnaire.dto.response.DoctorQuestionnaireAnalysisResponse;
import com.medflow.questionnaire.service.DoctorQuestionnairesService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
@RequestMapping("/api/v1/doctors/questionnaires")
public class DoctorQuestionnairesController {

    private final DoctorQuestionnairesService doctorQuestionnairesService;

    // 의사 담당 문진의 AI 분석 결과 조회
    @GetMapping("/{questionnaireId}/analysis")
    public ApiResponse<DoctorQuestionnaireAnalysisResponse> getQuestionnaireAnalysis(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long questionnaireId
    ) {
        return ApiResponse.success(
                doctorQuestionnairesService.getQuestionnaireAnalysis(
                        userDetails.getUserId(),
                        questionnaireId
                )
        );
    }
}
