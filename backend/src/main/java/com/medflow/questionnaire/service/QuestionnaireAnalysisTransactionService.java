package com.medflow.questionnaire.service;

import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import com.medflow.questionnaire.dto.request.QuestionnaireAnalysisRequest;
import com.medflow.questionnaire.dto.response.QuestionnaireAnalysisResponse;
import com.medflow.questionnaire.entity.Questionnaire;
import com.medflow.questionnaire.entity.QuestionnaireAnalysis;
import com.medflow.questionnaire.repository.QuestionnaireAnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuestionnaireAnalysisTransactionService {

    private final QuestionnaireAnalysisRepository questionnaireAnalysisRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public QuestionnaireAnalysisRequest startAnalysis(Long questionnaireId) {

        QuestionnaireAnalysis analysis = questionnaireAnalysisRepository.findByQuestionnaireId(questionnaireId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTIONNAIRE_ANALYSIS_NOT_FOUND));

        analysis.startProcessing();

        Questionnaire questionnaire = analysis.getQuestionnaire();

        return QuestionnaireAnalysisRequest.from(questionnaire);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completeAnalysis(
            Long questionnaireId,
            QuestionnaireAnalysisResponse result
    ) {

        QuestionnaireAnalysis analysis = questionnaireAnalysisRepository.findByQuestionnaireId(questionnaireId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTIONNAIRE_ANALYSIS_NOT_FOUND));

        analysis.complete(
                result.summary(),
                result.keyFindings(),
                result.riskSignals(),
                result.doctorCheckpoints(),
                result.priorityLevel()
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failAnalysis(Long questionnaireId) {

        QuestionnaireAnalysis analysis = questionnaireAnalysisRepository.findByQuestionnaireId(questionnaireId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTIONNAIRE_ANALYSIS_NOT_FOUND));

        analysis.fail();
    }
}
