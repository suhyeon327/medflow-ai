package com.medflow.questionnaire.service;

import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.entity.DoctorSchedule;
import com.medflow.doctor.repository.DoctorRepository;
import com.medflow.questionnaire.dto.response.DoctorQuestionnaireAnalysisResponse;
import com.medflow.questionnaire.entity.*;
import com.medflow.questionnaire.repository.QuestionnaireAnalysisRepository;
import com.medflow.questionnaire.repository.QuestionnaireRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorQuestionnairesServiceTest {

    @Mock DoctorRepository doctorRepository;
    @Mock QuestionnaireRepository questionnaireRepository;
    @Mock QuestionnaireAnalysisRepository questionnaireAnalysisRepository;
    @InjectMocks DoctorQuestionnairesService doctorQuestionnairesService;

    @Test
    void getQuestionnaireAnalysis_success_returnsIdsAndCompletedResult() {
        TestData data = testData(1L);
        data.analysis().startProcessing();
        data.analysis().complete(
                "기침과 발열이 있습니다.", List.of("지속된 기침"), List.of("발열 지속"),
                List.of("호흡곤란 확인"), PriorityLevel.CAUTION
        );
        mockFound(100L, data);

        DoctorQuestionnaireAnalysisResponse response =
                doctorQuestionnairesService.getQuestionnaireAnalysis(100L, 20L);

        assertThat(response.analysisId()).isEqualTo(30L);
        assertThat(response.questionnaireId()).isEqualTo(20L);
        assertThat(response.reservationId()).isEqualTo(10L);
        assertThat(response.summary()).isEqualTo("기침과 발열이 있습니다.");
        assertThat(response.keyFindings()).containsExactly("지속된 기침");
        assertThat(response.riskSignals()).containsExactly("발열 지속");
        assertThat(response.doctorCheckpoints()).containsExactly("호흡곤란 확인");
        assertThat(response.priorityLevel()).isEqualTo(PriorityLevel.CAUTION);
        assertThat(response.status()).isEqualTo(QuestionnaireAnalysisStatus.COMPLETED);
    }

    @Test
    void getQuestionnaireAnalysis_fails_forAnotherDoctorsQuestionnaire() {
        Doctor loginDoctor = doctor(1L);
        TestData data = testData(2L);
        when(doctorRepository.findByUserId(100L)).thenReturn(Optional.of(loginDoctor));
        when(questionnaireRepository.findById(20L)).thenReturn(Optional.of(data.questionnaire()));

        assertError(ErrorCode.QUESTIONNAIRE_RESERVATION_FORBIDDEN,
                () -> doctorQuestionnairesService.getQuestionnaireAnalysis(100L, 20L));
        verify(questionnaireAnalysisRepository, never()).findByQuestionnaireId(any());
    }

    @Test
    void getQuestionnaireAnalysis_fails_whenQuestionnaireDoesNotExist() {
        Doctor doctor = mock(Doctor.class);
        when(doctorRepository.findByUserId(100L)).thenReturn(Optional.of(doctor));
        when(questionnaireRepository.findById(999L)).thenReturn(Optional.empty());

        assertError(ErrorCode.QUESTIONNAIRE_NOT_FOUND,
                () -> doctorQuestionnairesService.getQuestionnaireAnalysis(100L, 999L));
    }

    @Test
    void getQuestionnaireAnalysis_fails_whenAnalysisDoesNotExist() {
        TestData data = testData(1L);
        when(doctorRepository.findByUserId(100L)).thenReturn(Optional.of(data.doctor()));
        when(questionnaireRepository.findById(20L)).thenReturn(Optional.of(data.questionnaire()));
        when(questionnaireAnalysisRepository.findByQuestionnaireId(20L)).thenReturn(Optional.empty());

        assertError(ErrorCode.QUESTIONNAIRE_ANALYSIS_NOT_FOUND,
                () -> doctorQuestionnairesService.getQuestionnaireAnalysis(100L, 20L));
    }

    @Test
    void getQuestionnaireAnalysis_returnsPendingState() {
        assertEmptyState(QuestionnaireAnalysisStatus.PENDING);
    }

    @Test
    void getQuestionnaireAnalysis_returnsProcessingState() {
        assertEmptyState(QuestionnaireAnalysisStatus.PROCESSING);
    }

    @Test
    void getQuestionnaireAnalysis_returnsFailedState() {
        assertEmptyState(QuestionnaireAnalysisStatus.FAILED);
    }

    private void assertEmptyState(QuestionnaireAnalysisStatus status) {
        TestData data = testData(1L);
        if (status == QuestionnaireAnalysisStatus.PROCESSING) {
            data.analysis().startProcessing();
        } else if (status == QuestionnaireAnalysisStatus.FAILED) {
            data.analysis().fail();
        }
        mockFound(100L, data);

        DoctorQuestionnaireAnalysisResponse response =
                doctorQuestionnairesService.getQuestionnaireAnalysis(100L, 20L);

        assertThat(response.status()).isEqualTo(status);
        assertThat(response.summary()).isNull();
        assertThat(response.keyFindings()).isEmpty();
        assertThat(response.riskSignals()).isEmpty();
        assertThat(response.doctorCheckpoints()).isEmpty();
        assertThat(response.priorityLevel()).isNull();
    }

    private void mockFound(Long userId, TestData data) {
        when(doctorRepository.findByUserId(userId)).thenReturn(Optional.of(data.doctor()));
        when(questionnaireRepository.findById(20L)).thenReturn(Optional.of(data.questionnaire()));
        when(questionnaireAnalysisRepository.findByQuestionnaireId(20L)).thenReturn(Optional.of(data.analysis()));
        when(data.reservation().getId()).thenReturn(10L);
    }

    private TestData testData(Long assignedDoctorId) {
        Doctor doctor = doctor(assignedDoctorId);
        DoctorSchedule schedule = mock(DoctorSchedule.class);
        when(schedule.getDoctor()).thenReturn(doctor);
        Reservation reservation = mock(Reservation.class);
        when(reservation.getDoctorSchedule()).thenReturn(schedule);
        Questionnaire questionnaire = Questionnaire.create(
                reservation, "기침", LocalDateTime.of(2026, 7, 28, 9, 0),
                "기침과 발열", 5, new BigDecimal("38.0"), "가래", null, null, null, null
        );
        ReflectionTestUtils.setField(questionnaire, "id", 20L);
        QuestionnaireAnalysis analysis = QuestionnaireAnalysis.pending(questionnaire);
        ReflectionTestUtils.setField(analysis, "id", 30L);
        return new TestData(doctor, reservation, questionnaire, analysis);
    }

    private Doctor doctor(Long id) {
        Doctor doctor = mock(Doctor.class);
        when(doctor.getId()).thenReturn(id);
        return doctor;
    }

    private void assertError(ErrorCode errorCode, Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }

    private record TestData(
            Doctor doctor,
            Reservation reservation,
            Questionnaire questionnaire,
            QuestionnaireAnalysis analysis
    ) {
    }
}
