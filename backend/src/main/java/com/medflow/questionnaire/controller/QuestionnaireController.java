package com.medflow.questionnaire.controller;

import com.medflow.security.principal.UserPrincipal;
import com.medflow.common.response.ApiResponse;
import com.medflow.questionnaire.dto.request.QuestionnaireCreateRequest;
import com.medflow.questionnaire.dto.request.QuestionnaireUpdateRequest;
import com.medflow.questionnaire.dto.response.QuestionnaireDetailResponse;
import com.medflow.questionnaire.dto.response.QuestionnaireAnalysisDetailResponse;
import com.medflow.questionnaire.dto.response.QuestionnaireResponse;
import com.medflow.questionnaire.dto.response.QuestionnaireUpdateResponse;
import com.medflow.questionnaire.service.QuestionnaireService;
import com.medflow.questionnaire.service.QuestionnaireAnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
@RequestMapping("/api/v1/questionnaires")
public class QuestionnaireController {

    private final QuestionnaireService questionnaireService;
    private final QuestionnaireAnalysisService questionnaireAnalysisService;

    // 예약 기반 문진 작성
    @PostMapping
    public ApiResponse<QuestionnaireResponse> createQuestionnaire(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody QuestionnaireCreateRequest request
    ) {
        return ApiResponse.success(
                questionnaireService.createQuestionnaire(userPrincipal.getUserId(), request)
        );
    }

    // 예약 문진 조회
    @GetMapping("/{reservationId}/questionnaire")
    public ApiResponse<QuestionnaireDetailResponse> getQuestionnaire(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long reservationId
    ) {
        return ApiResponse.success(
                questionnaireService.getQuestionnaire(userPrincipal.getUserId(), reservationId)
        );
    }

    // 예약 문진 수정
    @PutMapping("/{questionnaireId}")
    public ApiResponse<QuestionnaireUpdateResponse> updateQuestionnaire(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long questionnaireId,
            @Valid @RequestBody QuestionnaireUpdateRequest request
    ) {
        return ApiResponse.success(
                questionnaireService.updateQuestionnaire(userPrincipal.getUserId(), questionnaireId, request)
        );
    }

    // AI 문진 분석 결과 조회
    @GetMapping("/{questionnaireId}/analysis")
    public ApiResponse<QuestionnaireAnalysisDetailResponse> getQuestionnaireAnalysis(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long questionnaireId
    ) {
        return ApiResponse.success(
                questionnaireAnalysisService.getAnalysis(userPrincipal.getUserId(), questionnaireId)
        );
    }
}
