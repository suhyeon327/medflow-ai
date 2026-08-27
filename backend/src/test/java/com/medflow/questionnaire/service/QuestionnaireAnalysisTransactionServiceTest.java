package com.medflow.questionnaire.service;

import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import com.medflow.questionnaire.dto.request.QuestionnaireAnalysisRequest;
import com.medflow.questionnaire.dto.response.QuestionnaireAnalysisResponse;
import com.medflow.questionnaire.entity.PriorityLevel;
import com.medflow.questionnaire.entity.Questionnaire;
import com.medflow.questionnaire.entity.QuestionnaireAnalysis;
import com.medflow.questionnaire.entity.QuestionnaireAnalysisStatus;
import com.medflow.questionnaire.repository.QuestionnaireAnalysisRepository;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionnaireAnalysisTransactionServiceTest {

    @Mock QuestionnaireAnalysisRepository questionnaireAnalysisRepository;
    @InjectMocks QuestionnaireAnalysisTransactionService transactionService;

    @Test
    void startAnalysis_marksProcessingAndReturnsQuestionnaireSnapshot() {
        Questionnaire questionnaire = questionnaire();
        QuestionnaireAnalysis analysis = QuestionnaireAnalysis.pending(questionnaire);
        when(questionnaireAnalysisRepository.findByQuestionnaireId(20L)).thenReturn(Optional.of(analysis));

        QuestionnaireAnalysisRequest request = transactionService.startAnalysis(20L);

        assertThat(analysis.getStatus()).isEqualTo(QuestionnaireAnalysisStatus.PROCESSING);
        assertThat(request.questionnaireId()).isEqualTo(20L);
        assertThat(request.chiefComplaint()).isEqualTo("복통");
        assertThat(request.symptomStartedAt()).isEqualTo(LocalDateTime.of(2026, 7, 28, 9, 0));
        assertThat(request.temperature()).isEqualByComparingTo("37.5");
        assertThat(request.medicalHistory()).isEqualTo("고혈압");
    }

    @Test
    void completeAnalysis_appliesResultToExistingAnalysis() {
        QuestionnaireAnalysis analysis = QuestionnaireAnalysis.pending(questionnaire());
        QuestionnaireAnalysisResponse result = new QuestionnaireAnalysisResponse(
                "복통 종합 요약",
                List.of("통증과 체온 확인"),
                List.of("병력 종합 확인"),
                List.of("복부 상태 확인"),
                PriorityLevel.CAUTION
        );
        when(questionnaireAnalysisRepository.findByQuestionnaireId(20L)).thenReturn(Optional.of(analysis));

        transactionService.completeAnalysis(20L, result);

        assertThat(analysis.getStatus()).isEqualTo(QuestionnaireAnalysisStatus.COMPLETED);
        assertThat(analysis.getSummary()).isEqualTo("복통 종합 요약");
        assertThat(analysis.getKeyFindings()).containsExactly("통증과 체온 확인");
        assertThat(analysis.getRiskSignals()).containsExactly("병력 종합 확인");
        assertThat(analysis.getDoctorCheckpoints()).containsExactly("복부 상태 확인");
        assertThat(analysis.getPriorityLevel()).isEqualTo(PriorityLevel.CAUTION);
    }

    @Test
    void failAnalysis_clearsResultAndMarksFailed() {
        QuestionnaireAnalysis analysis = QuestionnaireAnalysis.pending(questionnaire());
        analysis.complete(
                "기존 요약",
                List.of("기존 소견"),
                List.of("기존 위험"),
                List.of("기존 확인사항"),
                PriorityLevel.NORMAL
        );
        when(questionnaireAnalysisRepository.findByQuestionnaireId(20L)).thenReturn(Optional.of(analysis));

        transactionService.failAnalysis(20L);

        assertThat(analysis.getStatus()).isEqualTo(QuestionnaireAnalysisStatus.FAILED);
        assertThat(analysis.getSummary()).isNull();
        assertThat(analysis.getKeyFindings()).isEmpty();
        assertThat(analysis.getRiskSignals()).isEmpty();
        assertThat(analysis.getDoctorCheckpoints()).isEmpty();
        assertThat(analysis.getPriorityLevel()).isNull();
    }

    @Test
    void startAnalysis_failsWhenAnalysisDoesNotExist() {
        when(questionnaireAnalysisRepository.findByQuestionnaireId(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.startAnalysis(999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.QUESTIONNAIRE_ANALYSIS_NOT_FOUND);
    }

    private Questionnaire questionnaire() {
        Questionnaire questionnaire = Questionnaire.create(
                mock(Reservation.class), "복통", LocalDateTime.of(2026, 7, 28, 9, 0),
                "배가 아픕니다.", 6, new BigDecimal("37.5"), "메스꺼움",
                "고혈압", "혈압약", "없음", "추가 사항"
        );
        ReflectionTestUtils.setField(questionnaire, "id", 20L);
        return questionnaire;
    }
}
