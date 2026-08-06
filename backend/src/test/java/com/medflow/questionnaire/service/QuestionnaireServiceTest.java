package com.medflow.questionnaire.service;

import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import com.medflow.doctor.entity.DoctorSchedule;
import com.medflow.patient.entity.Patient;
import com.medflow.patient.repository.PatientRepository;
import com.medflow.questionnaire.dto.request.QuestionnaireCreateRequest;
import com.medflow.questionnaire.dto.request.QuestionnaireUpdateRequest;
import com.medflow.questionnaire.dto.response.QuestionnaireDetailResponse;
import com.medflow.questionnaire.dto.response.QuestionnaireResponse;
import com.medflow.questionnaire.dto.response.QuestionnaireUpdateResponse;
import com.medflow.questionnaire.entity.Questionnaire;
import com.medflow.questionnaire.entity.QuestionnaireAnalysis;
import com.medflow.questionnaire.entity.QuestionnaireAnalysisStatus;
import com.medflow.questionnaire.event.QuestionnaireAnalysisRequestedEvent;
import com.medflow.questionnaire.repository.QuestionnaireAnalysisRepository;
import com.medflow.questionnaire.repository.QuestionnaireRepository;
import com.medflow.reservation.entity.Reservation;
import com.medflow.reservation.entity.ReservationStatus;
import com.medflow.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionnaireServiceTest {

    @Mock QuestionnaireRepository questionnaireRepository;
    @Mock QuestionnaireAnalysisRepository questionnaireAnalysisRepository;
    @Mock ReservationRepository reservationRepository;
    @Mock PatientRepository patientRepository;
    @Mock ApplicationEventPublisher eventPublisher;
    @InjectMocks QuestionnaireService questionnaireService;

    @Test
    void createQuestionnaire_success_and_savesAllFields() {
        Patient patient = patient(1L);
        Reservation reservation = reservation(10L, patient, ReservationStatus.APPROVED);
        QuestionnaireCreateRequest request = request(10L);
        mockFound(100L, patient, reservation);
        when(questionnaireRepository.save(any(Questionnaire.class))).thenAnswer(invocation -> {
            Questionnaire questionnaire = invocation.getArgument(0);
            ReflectionTestUtils.setField(questionnaire, "id", 20L);
            ReflectionTestUtils.setField(questionnaire, "createdAt", LocalDateTime.of(2026, 7, 29, 12, 0));
            return questionnaire;
        });

        QuestionnaireResponse response = questionnaireService.createQuestionnaire(100L, request);

        assertThat(response.questionnaireId()).isEqualTo(20L);
        assertThat(response.reservationId()).isEqualTo(10L);
        assertThat(response.patientId()).isEqualTo(1L);
        assertThat(response.chiefComplaint()).isEqualTo("복통");
        assertThat(response.symptomStartedAt()).isEqualTo(request.symptomStartedAt());
        assertThat(response.symptomDescription()).isEqualTo("배가 지속적으로 아픕니다.");
        assertThat(response.painLevel()).isEqualTo(7);
        assertThat(response.temperature()).isEqualByComparingTo("37.5");
        assertThat(response.associatedSymptoms()).isEqualTo("메스꺼움");
        assertThat(response.medicalHistory()).isEqualTo("고혈압");
        assertThat(response.medications()).isEqualTo("혈압약");
        assertThat(response.allergies()).isEqualTo("없음");
        assertThat(response.additionalNote()).isEqualTo("아침부터 악화됨");
        verify(questionnaireRepository).save(any(Questionnaire.class));
        verify(questionnaireAnalysisRepository).save(argThat(
                analysis -> analysis.getQuestionnaire().getId().equals(20L)
                        && analysis.getStatus() == QuestionnaireAnalysisStatus.PENDING
        ));
        verify(eventPublisher).publishEvent(new QuestionnaireAnalysisRequestedEvent(20L));
    }

    @Test
    void createQuestionnaire_fails_when_reservation_not_found() {
        Patient patient = mock(Patient.class);
        when(patientRepository.findByUserId(100L)).thenReturn(Optional.of(patient));
        when(reservationRepository.findById(10L)).thenReturn(Optional.empty());

        assertError(ErrorCode.RESERVATION_NOT_FOUND, () -> questionnaireService.createQuestionnaire(100L, request(10L)));
        verify(questionnaireRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void createQuestionnaire_fails_for_another_patients_reservation() {
        Patient loginPatient = patient(1L);
        Reservation reservation = reservation(10L, patient(2L), ReservationStatus.APPROVED);
        mockFound(100L, loginPatient, reservation);

        assertError(ErrorCode.QUESTIONNAIRE_RESERVATION_FORBIDDEN,
                () -> questionnaireService.createQuestionnaire(100L, request(10L)));
    }

    @Test
    void createQuestionnaire_fails_when_duplicate() {
        Patient patient = patient(1L);
        Reservation reservation = reservation(10L, patient, ReservationStatus.APPROVED);
        mockFound(100L, patient, reservation);
        when(questionnaireRepository.existsByReservationId(10L)).thenReturn(true);

        assertError(ErrorCode.QUESTIONNAIRE_ALREADY_EXISTS,
                () -> questionnaireService.createQuestionnaire(100L, request(10L)));
    }

    @Test
    void createQuestionnaire_fails_for_cancelled_reservation() {
        assertStatusFailure(ReservationStatus.CANCELLED, ErrorCode.QUESTIONNAIRE_CANCELLED_RESERVATION);
    }

    @Test
    void createQuestionnaire_fails_for_completed_reservation() {
        assertStatusFailure(ReservationStatus.COMPLETED, ErrorCode.QUESTIONNAIRE_COMPLETED_RESERVATION);
    }

    @Test
    void getQuestionnaire_success_and_returnsAllFields() {
        Patient patient = patient(1L);
        Reservation reservation = reservation(10L, patient, ReservationStatus.APPROVED);
        Questionnaire questionnaire = questionnaire(reservation);
        mockFound(100L, patient, reservation);
        when(questionnaireRepository.findByReservationId(10L)).thenReturn(Optional.of(questionnaire));

        QuestionnaireDetailResponse response = questionnaireService.getQuestionnaire(100L, 10L);

        assertThat(response.questionnaireId()).isEqualTo(20L);
        assertThat(response.reservationId()).isEqualTo(10L);
        assertThat(response.patientId()).isEqualTo(1L);
        assertThat(response.chiefComplaint()).isEqualTo("복통");
        assertThat(response.symptomStartedAt()).isEqualTo(LocalDateTime.of(2026, 7, 29, 9, 30));
        assertThat(response.symptomDescription()).isEqualTo("배가 지속적으로 아픕니다.");
        assertThat(response.painLevel()).isEqualTo(7);
        assertThat(response.temperature()).isEqualByComparingTo("37.5");
        assertThat(response.associatedSymptoms()).isEqualTo("메스꺼움");
        assertThat(response.medicalHistory()).isEqualTo("고혈압");
        assertThat(response.medications()).isEqualTo("혈압약");
        assertThat(response.allergies()).isEqualTo("없음");
        assertThat(response.additionalNote()).isEqualTo("아침부터 악화됨");
        assertThat(response.createdAt()).isEqualTo(LocalDateTime.of(2026, 7, 29, 12, 0));
        assertThat(response.updatedAt()).isEqualTo(LocalDateTime.of(2026, 7, 30, 9, 0));
    }

    @Test
    void getQuestionnaire_fails_for_another_patients_reservation() {
        Patient loginPatient = patient(1L);
        Reservation reservation = reservation(10L, patient(2L), ReservationStatus.APPROVED);
        mockFound(100L, loginPatient, reservation);

        assertError(ErrorCode.QUESTIONNAIRE_RESERVATION_FORBIDDEN,
                () -> questionnaireService.getQuestionnaire(100L, 10L));
        verify(questionnaireRepository, never()).findByReservationId(any());
    }

    @Test
    void getQuestionnaire_fails_when_reservation_not_found() {
        Patient patient = mock(Patient.class);
        when(patientRepository.findByUserId(100L)).thenReturn(Optional.of(patient));
        when(reservationRepository.findById(10L)).thenReturn(Optional.empty());

        assertError(ErrorCode.RESERVATION_NOT_FOUND,
                () -> questionnaireService.getQuestionnaire(100L, 10L));
        verify(questionnaireRepository, never()).findByReservationId(any());
    }

    @Test
    void getQuestionnaire_fails_when_questionnaire_not_found() {
        Patient patient = patient(1L);
        Reservation reservation = reservation(10L, patient, ReservationStatus.APPROVED);
        mockFound(100L, patient, reservation);
        when(questionnaireRepository.findByReservationId(10L)).thenReturn(Optional.empty());

        assertError(ErrorCode.QUESTIONNAIRE_NOT_FOUND,
                () -> questionnaireService.getQuestionnaire(100L, 10L));
    }

    @Test
    void updateQuestionnaire_success_updatesAllFields_and_resetsAnalysisToPending() {
        Patient patient = patient(1L);
        Reservation reservation = updatableReservation(10L, patient, ReservationStatus.APPROVED);
        Questionnaire questionnaire = questionnaire(reservation);
        QuestionnaireAnalysis analysis = QuestionnaireAnalysis.pending(questionnaire);
        ReflectionTestUtils.setField(analysis, "status", QuestionnaireAnalysisStatus.COMPLETED);
        QuestionnaireUpdateRequest request = updateRequest(8);

        when(patientRepository.findByUserId(100L)).thenReturn(Optional.of(patient));
        when(questionnaireRepository.findById(20L)).thenReturn(Optional.of(questionnaire));
        when(questionnaireAnalysisRepository.findByQuestionnaireId(20L)).thenReturn(Optional.of(analysis));
        when(questionnaireRepository.saveAndFlush(questionnaire)).thenAnswer(invocation -> {
            ReflectionTestUtils.setField(questionnaire, "updatedAt", LocalDateTime.of(2026, 7, 30, 15, 0));
            return questionnaire;
        });

        QuestionnaireUpdateResponse response = questionnaireService.updateQuestionnaire(100L, 20L, request);

        assertThat(response.questionnaireId()).isEqualTo(20L);
        assertThat(response.reservationId()).isEqualTo(10L);
        assertThat(response.patientId()).isEqualTo(1L);
        assertThat(response.chiefComplaint()).isEqualTo("두통");
        assertThat(response.symptomStartedAt()).isEqualTo(request.symptomStartedAt());
        assertThat(response.symptomDescription()).isEqualTo("머리가 심하게 아픕니다.");
        assertThat(response.painLevel()).isEqualTo(8);
        assertThat(response.temperature()).isEqualByComparingTo("38.1");
        assertThat(response.associatedSymptoms()).isEqualTo("어지러움");
        assertThat(response.medicalHistory()).isEqualTo("당뇨");
        assertThat(response.medications()).isEqualTo("당뇨약");
        assertThat(response.allergies()).isEqualTo("페니실린");
        assertThat(response.additionalNote()).isEqualTo("오후부터 악화됨");
        assertThat(response.updatedAt()).isEqualTo(LocalDateTime.of(2026, 7, 30, 15, 0));
        assertThat(analysis.getStatus()).isEqualTo(QuestionnaireAnalysisStatus.PENDING);
        verify(questionnaireAnalysisRepository, never()).save(any());
        verify(eventPublisher).publishEvent(new QuestionnaireAnalysisRequestedEvent(20L));
    }

    @Test
    void updateQuestionnaire_fails_when_analysis_does_not_exist() {
        Patient patient = patient(1L);
        Reservation reservation = updatableReservation(10L, patient, ReservationStatus.APPROVED);
        Questionnaire questionnaire = questionnaire(reservation);
        when(patientRepository.findByUserId(100L)).thenReturn(Optional.of(patient));
        when(questionnaireRepository.findById(20L)).thenReturn(Optional.of(questionnaire));
        when(questionnaireAnalysisRepository.findByQuestionnaireId(20L)).thenReturn(Optional.empty());

        assertError(ErrorCode.QUESTIONNAIRE_ANALYSIS_NOT_FOUND,
                () -> questionnaireService.updateQuestionnaire(100L, 20L, updateRequest(5)));
        verify(questionnaireAnalysisRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void updateQuestionnaire_fails_for_another_patients_questionnaire() {
        Patient loginPatient = patient(1L);
        Questionnaire questionnaire = questionnaire(updatableReservation(10L, patient(2L), ReservationStatus.APPROVED));
        when(patientRepository.findByUserId(100L)).thenReturn(Optional.of(loginPatient));
        when(questionnaireRepository.findById(20L)).thenReturn(Optional.of(questionnaire));

        assertError(ErrorCode.QUESTIONNAIRE_RESERVATION_FORBIDDEN,
                () -> questionnaireService.updateQuestionnaire(100L, 20L, updateRequest(5)));
    }

    @Test
    void updateQuestionnaire_fails_when_questionnaire_not_found() {
        when(patientRepository.findByUserId(100L)).thenReturn(Optional.of(mock(Patient.class)));
        when(questionnaireRepository.findById(999L)).thenReturn(Optional.empty());

        assertError(ErrorCode.QUESTIONNAIRE_NOT_FOUND,
                () -> questionnaireService.updateQuestionnaire(100L, 999L, updateRequest(5)));
    }

    @Test
    void updateQuestionnaire_fails_for_completed_reservation() {
        assertUpdateStatusFailure(ReservationStatus.COMPLETED, ErrorCode.QUESTIONNAIRE_COMPLETED_RESERVATION);
    }

    @Test
    void updateQuestionnaire_fails_for_cancelled_reservation() {
        assertUpdateStatusFailure(ReservationStatus.CANCELLED, ErrorCode.QUESTIONNAIRE_CANCELLED_RESERVATION);
    }

    @Test
    void updateQuestionnaire_fails_after_appointment_start() {
        Patient patient = patient(1L);
        Reservation reservation = updatableReservation(10L, patient, ReservationStatus.APPROVED);
        when(reservation.getDoctorSchedule().getDate()).thenReturn(LocalDate.of(2020, 1, 1));
        Questionnaire questionnaire = questionnaire(reservation);
        when(patientRepository.findByUserId(100L)).thenReturn(Optional.of(patient));
        when(questionnaireRepository.findById(20L)).thenReturn(Optional.of(questionnaire));

        assertError(ErrorCode.QUESTIONNAIRE_UPDATE_AFTER_START,
                () -> questionnaireService.updateQuestionnaire(100L, 20L, updateRequest(5)));
    }

    private void assertUpdateStatusFailure(ReservationStatus status, ErrorCode errorCode) {
        Patient patient = patient(1L);
        Questionnaire questionnaire = questionnaire(updatableReservation(10L, patient, status));
        when(patientRepository.findByUserId(100L)).thenReturn(Optional.of(patient));
        when(questionnaireRepository.findById(20L)).thenReturn(Optional.of(questionnaire));
        assertError(errorCode, () -> questionnaireService.updateQuestionnaire(100L, 20L, updateRequest(5)));
        verify(questionnaireAnalysisRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    private void assertStatusFailure(ReservationStatus status, ErrorCode errorCode) {
        Patient patient = patient(1L);
        Reservation reservation = reservation(10L, patient, status);
        mockFound(100L, patient, reservation);
        assertError(errorCode, () -> questionnaireService.createQuestionnaire(100L, request(10L)));
        verify(questionnaireRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    private void assertError(ErrorCode errorCode, Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }

    private void mockFound(Long userId, Patient patient, Reservation reservation) {
        when(patientRepository.findByUserId(userId)).thenReturn(Optional.of(patient));
        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
    }

    private Patient patient(Long id) {
        Patient patient = mock(Patient.class);
        when(patient.getId()).thenReturn(id);
        return patient;
    }

    private Reservation reservation(Long id, Patient patient, ReservationStatus status) {
        Reservation reservation = mock(Reservation.class);
        when(reservation.getId()).thenReturn(id);
        when(reservation.getPatient()).thenReturn(patient);
        lenient().when(reservation.getStatus()).thenReturn(status);
        return reservation;
    }

    private Reservation updatableReservation(Long id, Patient patient, ReservationStatus status) {
        Reservation reservation = reservation(id, patient, status);
        DoctorSchedule schedule = mock(DoctorSchedule.class);
        lenient().when(schedule.getDate()).thenReturn(LocalDate.of(2099, 1, 1));
        lenient().when(schedule.getStartTime()).thenReturn(LocalTime.of(10, 0));
        lenient().when(reservation.getDoctorSchedule()).thenReturn(schedule);
        return reservation;
    }

    private QuestionnaireCreateRequest request(Long reservationId) {
        return new QuestionnaireCreateRequest(
                reservationId, "복통", LocalDateTime.of(2026, 7, 29, 9, 30),
                "배가 지속적으로 아픕니다.", 7, new BigDecimal("37.5"),
                "메스꺼움", "고혈압", "혈압약", "없음", "아침부터 악화됨"
        );
    }

    private Questionnaire questionnaire(Reservation reservation) {
        QuestionnaireCreateRequest request = request(reservation.getId());
        Questionnaire questionnaire = Questionnaire.create(
                reservation, request.chiefComplaint(), request.symptomStartedAt(),
                request.symptomDescription(), request.painLevel(), request.temperature(),
                request.associatedSymptoms(), request.medicalHistory(), request.medications(),
                request.allergies(), request.additionalNote()
        );
        ReflectionTestUtils.setField(questionnaire, "id", 20L);
        ReflectionTestUtils.setField(questionnaire, "createdAt", LocalDateTime.of(2026, 7, 29, 12, 0));
        ReflectionTestUtils.setField(questionnaire, "updatedAt", LocalDateTime.of(2026, 7, 30, 9, 0));
        return questionnaire;
    }

    private QuestionnaireUpdateRequest updateRequest(Integer painLevel) {
        return new QuestionnaireUpdateRequest(
                "두통", LocalDateTime.of(2026, 7, 30, 8, 0), "머리가 심하게 아픕니다.",
                painLevel, new BigDecimal("38.1"), "어지러움", "당뇨", "당뇨약",
                "페니실린", "오후부터 악화됨"
        );
    }
}
