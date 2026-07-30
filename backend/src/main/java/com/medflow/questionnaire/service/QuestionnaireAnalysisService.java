package com.medflow.questionnaire.service;

import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import com.medflow.questionnaire.analysis.AiQuestionnaireAnalyzer;
import com.medflow.questionnaire.dto.response.QuestionnaireAnalysisResponse;
import com.medflow.questionnaire.dto.response.QuestionnaireAnalysisDetailResponse;
import com.medflow.patient.entity.Patient;
import com.medflow.patient.repository.PatientRepository;
import com.medflow.questionnaire.entity.Questionnaire;
import com.medflow.questionnaire.entity.QuestionnaireAnalysis;
import com.medflow.questionnaire.repository.QuestionnaireAnalysisRepository;
import com.medflow.questionnaire.repository.QuestionnaireRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuestionnaireAnalysisService {

    private final QuestionnaireAnalysisRepository questionnaireAnalysisRepository;
    private final QuestionnaireRepository questionnaireRepository;
    private final PatientRepository patientRepository;
    private final AiQuestionnaireAnalyzer aiQuestionnaireAnalyzer;

    // 환자 문진 분석 결과 조회
    @Transactional(readOnly = true)
    public QuestionnaireAnalysisDetailResponse getAnalysis(Long userId, Long questionnaireId) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PATIENT_NOT_FOUND));

        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTIONNAIRE_NOT_FOUND));

        if (!questionnaire.getReservation().getPatient().getId().equals(patient.getId())) {
            throw new BusinessException(ErrorCode.QUESTIONNAIRE_RESERVATION_FORBIDDEN);
        }

        QuestionnaireAnalysis analysis = questionnaireAnalysisRepository.findByQuestionnaireId(questionnaireId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTIONNAIRE_ANALYSIS_NOT_FOUND));

        return QuestionnaireAnalysisDetailResponse.from(analysis);
    }
  
    // 환자 문진 분석
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void analyze(Long questionnaireId) {

        QuestionnaireAnalysis analysis = questionnaireAnalysisRepository.findByQuestionnaireId(questionnaireId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTIONNAIRE_ANALYSIS_NOT_FOUND));

        analysis.startProcessing();

        try {
            QuestionnaireAnalysisResponse result = aiQuestionnaireAnalyzer.analyze(analysis.getQuestionnaire());
            analysis.complete(
                    result.summary(),
                    result.keyFindings(),
                    result.riskSignals(),
                    result.doctorCheckpoints(),
                    result.urgencyLevel()
            );
        } catch (Exception e) {
            log.error(
                    "AI 문진 분석 실패. questionnaireId={}",
                    questionnaireId,
                    e
            );

            analysis.fail();
        }
    }
}
