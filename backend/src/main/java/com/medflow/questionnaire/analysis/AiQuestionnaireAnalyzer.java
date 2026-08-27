package com.medflow.questionnaire.analysis;

import com.medflow.questionnaire.dto.request.QuestionnaireAnalysisRequest;
import com.medflow.questionnaire.dto.response.QuestionnaireAnalysisResponse;

public interface AiQuestionnaireAnalyzer {

    QuestionnaireAnalysisResponse analyze(QuestionnaireAnalysisRequest request);
}
