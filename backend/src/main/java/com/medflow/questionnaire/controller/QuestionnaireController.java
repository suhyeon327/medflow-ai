package com.medflow.questionnaire.controller;

import com.medflow.auth.security.CustomUserDetails;
import com.medflow.common.response.ApiResponse;
import com.medflow.questionnaire.dto.request.QuestionnaireCreateRequest;
import com.medflow.questionnaire.dto.request.QuestionnaireUpdateRequest;
import com.medflow.questionnaire.dto.response.QuestionnaireDetailResponse;
import com.medflow.questionnaire.dto.response.QuestionnaireResponse;
import com.medflow.questionnaire.dto.response.QuestionnaireUpdateResponse;
import com.medflow.questionnaire.service.QuestionnaireService;
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

    // 예약 기반 문진 작성
    @PostMapping
    public ApiResponse<QuestionnaireResponse> createQuestionnaire(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody QuestionnaireCreateRequest request
    ) {
        return ApiResponse.success(
                questionnaireService.createQuestionnaire(userDetails.getUserId(), request)
        );
    }

    // 예약 문진 조회
    @GetMapping("/{reservationId}")
    public ApiResponse<QuestionnaireDetailResponse> getQuestionnaire(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reservationId
    ) {
        return ApiResponse.success(
                questionnaireService.getQuestionnaire(userDetails.getUserId(), reservationId)
        );
    }

    // 예약 문진 수정
    @PutMapping("/{questionnaireId}")
    public ApiResponse<QuestionnaireUpdateResponse> updateQuestionnaire(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long questionnaireId,
            @Valid @RequestBody QuestionnaireUpdateRequest request
    ) {
        return ApiResponse.success(
                questionnaireService.updateQuestionnaire(userDetails.getUserId(), questionnaireId, request)
        );
    }
}
