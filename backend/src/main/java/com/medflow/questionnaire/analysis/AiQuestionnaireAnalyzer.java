package com.medflow.questionnaire.analysis;

import com.medflow.questionnaire.dto.response.QuestionnaireAnalysisResponse;
import com.medflow.questionnaire.entity.Questionnaire;

public interface AiQuestionnaireAnalyzer {

    QuestionnaireAnalysisResponse analyze(Questionnaire questionnaire);
}
