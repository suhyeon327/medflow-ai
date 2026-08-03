package com.medflow.questionnaire.dto.response;

import com.medflow.questionnaire.entity.PriorityLevel;

import java.util.List;

public record QuestionnaireAnalysisResponse(
        String summary,
        List<String> keyFindings,
        List<String> riskSignals,
        List<String> doctorCheckpoints,
        PriorityLevel priorityLevel
) {
}
