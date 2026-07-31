package com.medflow.questionnaire.dto.response;

import com.medflow.questionnaire.entity.QuestionnaireAnalysis;
import com.medflow.questionnaire.entity.QuestionnaireAnalysisStatus;
import com.medflow.questionnaire.entity.UrgencyLevel;

import java.util.List;

public record DoctorQuestionnaireAnalysisResponse(
        Long analysisId,
        Long questionnaireId,
        Long reservationId,
        String summary,
        List<String> keyFindings,
        List<String> riskSignals,
        List<String> doctorCheckpoints,
        UrgencyLevel priorityLevel,
        QuestionnaireAnalysisStatus status
) {
    public static DoctorQuestionnaireAnalysisResponse from(QuestionnaireAnalysis analysis) {
        return new DoctorQuestionnaireAnalysisResponse(
                analysis.getId(),
                analysis.getQuestionnaire().getId(),
                analysis.getQuestionnaire().getReservation().getId(),
                analysis.getSummary(),
                List.copyOf(analysis.getKeyFindings()),
                List.copyOf(analysis.getRiskSignals()),
                List.copyOf(analysis.getDoctorCheckpoints()),
                analysis.getPriorityLevel(),
                analysis.getStatus()
        );
    }
}
