package com.medflow.questionnaire.controller;

import com.medflow.auth.security.CustomUserDetails;
import com.medflow.common.response.ApiResponse;
import com.medflow.questionnaire.dto.request.QuestionnaireCreateRequest;
import com.medflow.questionnaire.dto.response.QuestionnaireResponse;
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
}
