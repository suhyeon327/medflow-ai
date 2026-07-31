package com.medflow.questionnaire.service;

import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.repository.DoctorRepository;
import com.medflow.questionnaire.dto.response.DoctorQuestionnaireAnalysisResponse;
import com.medflow.questionnaire.entity.Questionnaire;
import com.medflow.questionnaire.entity.QuestionnaireAnalysis;
import com.medflow.questionnaire.repository.QuestionnaireAnalysisRepository;
import com.medflow.questionnaire.repository.QuestionnaireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorQuestionnairesService {

    private final DoctorRepository doctorRepository;
    private final QuestionnaireRepository questionnaireRepository;
    private final QuestionnaireAnalysisRepository questionnaireAnalysisRepository;

    // 의사 담당 문진의 AI 분석 결과 조회
    public DoctorQuestionnaireAnalysisResponse getQuestionnaireAnalysis(
            Long userId,
            Long questionnaireId
    ) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCTOR_NOT_FOUND));

        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTIONNAIRE_NOT_FOUND));

        if (!questionnaire.getReservation().getDoctorSchedule().getDoctor().getId().equals(doctor.getId())) {
            throw new BusinessException(ErrorCode.QUESTIONNAIRE_RESERVATION_FORBIDDEN);
        }

        QuestionnaireAnalysis analysis = questionnaireAnalysisRepository
                .findByQuestionnaireId(questionnaireId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTIONNAIRE_ANALYSIS_NOT_FOUND));

        return DoctorQuestionnaireAnalysisResponse.from(analysis);
    }
}
