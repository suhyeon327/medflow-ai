package com.medflow.questionnaire.dto.analysis;

import java.util.List;

public record GeminiAnalysisResult(
        String summary,
        List<String> keyFindings,
        List<String> riskSignals,
        List<String> doctorCheckpoints,
        String priorityLevel
) {
}
