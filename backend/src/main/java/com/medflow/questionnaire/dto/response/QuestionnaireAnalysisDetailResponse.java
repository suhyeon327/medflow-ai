package com.medflow.questionnaire.dto.response;

import com.medflow.questionnaire.entity.QuestionnaireAnalysis;
import com.medflow.questionnaire.entity.QuestionnaireAnalysisStatus;
import com.medflow.questionnaire.entity.PriorityLevel;

import java.util.List;

public record QuestionnaireAnalysisDetailResponse(
        Long analysisId,
        Long questionnaireId,
        String summary,
        List<String> keyFindings,
        List<String> riskSignals,
        List<String> doctorCheckpoints,
        PriorityLevel priorityLevel,
        QuestionnaireAnalysisStatus status
) {
    public static QuestionnaireAnalysisDetailResponse from(QuestionnaireAnalysis analysis) {
        return new QuestionnaireAnalysisDetailResponse(
                analysis.getId(),
                analysis.getQuestionnaire().getId(),
                analysis.getSummary(),
                List.copyOf(analysis.getKeyFindings()),
                List.copyOf(analysis.getRiskSignals()),
                List.copyOf(analysis.getDoctorCheckpoints()),
                analysis.getPriorityLevel(),
                analysis.getStatus()
        );
    }
}
