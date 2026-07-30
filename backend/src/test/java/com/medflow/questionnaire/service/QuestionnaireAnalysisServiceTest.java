package com.medflow.questionnaire.service;

import com.medflow.questionnaire.analysis.AiQuestionnaireAnalyzer;
import com.medflow.questionnaire.dto.response.QuestionnaireAnalysisResponse;
import com.medflow.questionnaire.dto.response.QuestionnaireAnalysisDetailResponse;
import com.medflow.questionnaire.entity.*;
import com.medflow.questionnaire.repository.QuestionnaireAnalysisRepository;
import com.medflow.questionnaire.repository.QuestionnaireRepository;
import com.medflow.patient.entity.Patient;
import com.medflow.patient.repository.PatientRepository;
import com.medflow.reservation.entity.Reservation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionnaireAnalysisServiceTest {

    @Mock QuestionnaireAnalysisRepository questionnaireAnalysisRepository;
    @Mock QuestionnaireRepository questionnaireRepository;
    @Mock PatientRepository patientRepository;
    @Mock AiQuestionnaireAnalyzer aiQuestionnaireAnalyzer;
    @InjectMocks QuestionnaireAnalysisService questionnaireAnalysisService;

    @Test
    void analyze_success_savesCompletedResultOnExistingAnalysis() {
        Questionnaire questionnaire = questionnaire("복통");
        QuestionnaireAnalysis analysis = QuestionnaireAnalysis.pending(questionnaire);
        QuestionnaireAnalysisResponse result = result("복통");
        when(questionnaireAnalysisRepository.findByQuestionnaireId(20L)).thenReturn(Optional.of(analysis));
        when(aiQuestionnaireAnalyzer.analyze(questionnaire)).thenReturn(result);

        questionnaireAnalysisService.analyze(20L);

        assertThat(analysis.getStatus()).isEqualTo(QuestionnaireAnalysisStatus.COMPLETED);
        assertThat(analysis.getSummary()).isEqualTo("복통 종합 요약");
        assertThat(analysis.getKeyFindings()).containsExactly("통증과 체온 확인");
        assertThat(analysis.getRiskSignals()).containsExactly("병력 종합 확인");
        assertThat(analysis.getDoctorCheckpoints()).containsExactly("복부 상태 확인");
        assertThat(analysis.getPriorityLevel()).isEqualTo(com.medflow.questionnaire.entity.UrgencyLevel.NORMAL);
        assertThat(analysis.getStatus()).isEqualTo(QuestionnaireAnalysisStatus.COMPLETED);
        verify(questionnaireAnalysisRepository, never()).save(any());
    }

    @Test
    void analyze_afterQuestionnaireUpdate_reflectsLatestQuestionnaireData() {
        Questionnaire questionnaire = questionnaire("복통");
        questionnaire.update(
                "두통", LocalDateTime.of(2026, 7, 30, 8, 0), "머리가 아픕니다.",
                8, new BigDecimal("38.1"), "어지러움", "당뇨", "당뇨약",
                "페니실린", "오후부터 악화됨"
        );
        QuestionnaireAnalysis analysis = QuestionnaireAnalysis.pending(questionnaire);
        when(questionnaireAnalysisRepository.findByQuestionnaireId(20L)).thenReturn(Optional.of(analysis));
        when(aiQuestionnaireAnalyzer.analyze(questionnaire)).thenReturn(result(questionnaire.getChiefComplaint()));

        questionnaireAnalysisService.analyze(20L);

        assertThat(analysis.getSummary()).isEqualTo("두통 종합 요약");
        verify(questionnaireAnalysisRepository, never()).save(any());
    }

    @Test
    void analyze_failure_marksFailed_withoutThrowing() {
        Questionnaire questionnaire = questionnaire("복통");
        QuestionnaireAnalysis analysis = QuestionnaireAnalysis.pending(questionnaire);
        when(questionnaireAnalysisRepository.findByQuestionnaireId(20L)).thenReturn(Optional.of(analysis));
        when(aiQuestionnaireAnalyzer.analyze(questionnaire)).thenThrow(new IllegalStateException("Fake 분석 실패"));

        questionnaireAnalysisService.analyze(20L);

        assertThat(analysis.getStatus()).isEqualTo(QuestionnaireAnalysisStatus.FAILED);
        assertThat(analysis.getSummary()).isNull();
        assertThat(analysis.getKeyFindings()).isEmpty();
    }

    @Test
    void getAnalysis_success_returnsOwnQuestionnaireAnalysis() {
        Patient patient = patient(1L);
        Questionnaire questionnaire = questionnaire(20L, patient);
        QuestionnaireAnalysis analysis = analysis(30L, questionnaire);
        mockAnalysisFound(100L, patient, questionnaire, analysis);

        QuestionnaireAnalysisDetailResponse response = questionnaireAnalysisService.getAnalysis(100L, 20L);

        assertThat(response.analysisId()).isEqualTo(30L);
        assertThat(response.questionnaireId()).isEqualTo(20L);
        assertThat(response.status()).isEqualTo(QuestionnaireAnalysisStatus.PENDING);
    }

    @Test
    void getAnalysis_fails_forAnotherPatientsQuestionnaire() {
        Patient loginPatient = patient(1L);
        Questionnaire questionnaire = questionnaire(20L, patient(2L));
        when(patientRepository.findByUserId(100L)).thenReturn(Optional.of(loginPatient));
        when(questionnaireRepository.findById(20L)).thenReturn(Optional.of(questionnaire));

        assertThatThrownBy(() -> questionnaireAnalysisService.getAnalysis(100L, 20L))
                .isInstanceOf(com.medflow.common.exception.BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(com.medflow.common.exception.ErrorCode.QUESTIONNAIRE_RESERVATION_FORBIDDEN);
        verify(questionnaireAnalysisRepository, never()).findByQuestionnaireId(any());
    }

    @Test
    void getAnalysis_fails_whenQuestionnaireDoesNotExist() {
        Patient patient = mock(Patient.class);
        when(patientRepository.findByUserId(100L)).thenReturn(Optional.of(patient));
        when(questionnaireRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questionnaireAnalysisService.getAnalysis(100L, 999L))
                .isInstanceOf(com.medflow.common.exception.BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(com.medflow.common.exception.ErrorCode.QUESTIONNAIRE_NOT_FOUND);
    }

    @Test
    void getAnalysis_fails_whenAnalysisDoesNotExist() {
        Patient patient = patient(1L);
        Questionnaire questionnaire = questionnaire(20L, patient);
        when(patientRepository.findByUserId(100L)).thenReturn(Optional.of(patient));
        when(questionnaireRepository.findById(20L)).thenReturn(Optional.of(questionnaire));
        when(questionnaireAnalysisRepository.findByQuestionnaireId(20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questionnaireAnalysisService.getAnalysis(100L, 20L))
                .isInstanceOf(com.medflow.common.exception.BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(com.medflow.common.exception.ErrorCode.QUESTIONNAIRE_ANALYSIS_NOT_FOUND);
    }

    @Test
    void getAnalysis_pending_returnsEmptyResult() {
        assertEmptyStateResponse(QuestionnaireAnalysisStatus.PENDING);
    }

    @Test
    void getAnalysis_processing_returnsEmptyResult() {
        assertEmptyStateResponse(QuestionnaireAnalysisStatus.PROCESSING);
    }

    @Test
    void getAnalysis_completed_returnsAllResultFields() {
        Patient patient = patient(1L);
        Questionnaire questionnaire = questionnaire(20L, patient);
        QuestionnaireAnalysis analysis = analysis(30L, questionnaire);
        analysis.startProcessing();
        analysis.complete(
                "3일 전부터 기침과 발열이 있습니다.",
                List.of("3일간 지속된 기침", "38도의 발열"),
                List.of("발열 지속"),
                List.of("호흡곤란 여부 확인", "발열 지속 시간 확인"),
                UrgencyLevel.CAUTION
        );
        mockAnalysisFound(100L, patient, questionnaire, analysis);

        QuestionnaireAnalysisDetailResponse response = questionnaireAnalysisService.getAnalysis(100L, 20L);

        assertThat(response.summary()).isEqualTo("3일 전부터 기침과 발열이 있습니다.");
        assertThat(response.keyFindings()).containsExactly("3일간 지속된 기침", "38도의 발열");
        assertThat(response.riskSignals()).containsExactly("발열 지속");
        assertThat(response.doctorCheckpoints()).containsExactly("호흡곤란 여부 확인", "발열 지속 시간 확인");
        assertThat(response.priorityLevel()).isEqualTo(UrgencyLevel.CAUTION);
        assertThat(response.status()).isEqualTo(QuestionnaireAnalysisStatus.COMPLETED);
    }

    @Test
    void getAnalysis_failed_returnsEmptyResult() {
        Patient patient = patient(1L);
        Questionnaire questionnaire = questionnaire(20L, patient);
        QuestionnaireAnalysis analysis = analysis(30L, questionnaire);
        analysis.startProcessing();
        analysis.fail();
        mockAnalysisFound(100L, patient, questionnaire, analysis);

        QuestionnaireAnalysisDetailResponse response = questionnaireAnalysisService.getAnalysis(100L, 20L);

        assertThat(response.status()).isEqualTo(QuestionnaireAnalysisStatus.FAILED);
        assertThat(response.summary()).isNull();
        assertThat(response.keyFindings()).isEmpty();
        assertThat(response.riskSignals()).isEmpty();
        assertThat(response.doctorCheckpoints()).isEmpty();
        assertThat(response.priorityLevel()).isNull();
    }

    private void assertEmptyStateResponse(QuestionnaireAnalysisStatus status) {
        Patient patient = patient(1L);
        Questionnaire questionnaire = questionnaire(20L, patient);
        QuestionnaireAnalysis analysis = analysis(30L, questionnaire);
        if (status == QuestionnaireAnalysisStatus.PROCESSING) {
            analysis.startProcessing();
        }
        mockAnalysisFound(100L, patient, questionnaire, analysis);

        QuestionnaireAnalysisDetailResponse response = questionnaireAnalysisService.getAnalysis(100L, 20L);

        assertThat(response.status()).isEqualTo(status);
        assertThat(response.summary()).isNull();
        assertThat(response.keyFindings()).isEmpty();
        assertThat(response.riskSignals()).isEmpty();
        assertThat(response.doctorCheckpoints()).isEmpty();
        assertThat(response.priorityLevel()).isNull();
    }

    private void mockAnalysisFound(
            Long userId,
            Patient patient,
            Questionnaire questionnaire,
            QuestionnaireAnalysis analysis
    ) {
        when(patientRepository.findByUserId(userId)).thenReturn(Optional.of(patient));
        when(questionnaireRepository.findById(questionnaire.getId())).thenReturn(Optional.of(questionnaire));
        when(questionnaireAnalysisRepository.findByQuestionnaireId(questionnaire.getId()))
                .thenReturn(Optional.of(analysis));
    }

    private Patient patient(Long id) {
        Patient patient = mock(Patient.class);
        when(patient.getId()).thenReturn(id);
        return patient;
    }

    private Questionnaire questionnaire(Long id, Patient patient) {
        Reservation reservation = mock(Reservation.class);
        when(reservation.getPatient()).thenReturn(patient);
        Questionnaire questionnaire = Questionnaire.create(
                reservation, "기침", LocalDateTime.of(2026, 7, 28, 9, 0),
                "상세 증상", 5, new BigDecimal("38.0"), "발열",
                null, null, null, null
        );
        ReflectionTestUtils.setField(questionnaire, "id", id);
        return questionnaire;
    }

    private QuestionnaireAnalysis analysis(Long id, Questionnaire questionnaire) {
        QuestionnaireAnalysis analysis = QuestionnaireAnalysis.pending(questionnaire);
        ReflectionTestUtils.setField(analysis, "id", id);
        return analysis;
    }

    private Questionnaire questionnaire(String chiefComplaint) {
        return Questionnaire.create(
                mock(Reservation.class), chiefComplaint, LocalDateTime.of(2026, 7, 28, 9, 0),
                "상세 증상", 7, new BigDecimal("37.5"), "메스꺼움",
                "고혈압", "혈압약", "없음", "추가 사항"
        );
    }

    private QuestionnaireAnalysisResponse result(String chiefComplaint) {
        return new QuestionnaireAnalysisResponse(
                chiefComplaint + " 종합 요약",
                List.of("통증과 체온 확인"),
                List.of("병력 종합 확인"),
                List.of("복부 상태 확인"),
                com.medflow.questionnaire.entity.UrgencyLevel.NORMAL
        );
    }
}
