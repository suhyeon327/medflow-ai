package com.medflow.questionnaire.service;

import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import com.medflow.patient.entity.Patient;
import com.medflow.patient.repository.PatientRepository;
import com.medflow.questionnaire.dto.request.QuestionnaireCreateRequest;
import com.medflow.questionnaire.dto.response.QuestionnaireResponse;
import com.medflow.questionnaire.entity.Questionnaire;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionnaireServiceTest {

    @Mock QuestionnaireRepository questionnaireRepository;
    @Mock ReservationRepository reservationRepository;
    @Mock PatientRepository patientRepository;
    @InjectMocks QuestionnaireService questionnaireService;

    @Test
    void createQuestionnaire_success_and_savesAllFields() {
        Patient patient = patient(1L);
        Reservation reservation = reservation(10L, patient, ReservationStatus.CONFIRMED);
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
    }

    @Test
    void createQuestionnaire_fails_when_reservation_not_found() {
        Patient patient = mock(Patient.class);
        when(patientRepository.findByUserId(100L)).thenReturn(Optional.of(patient));
        when(reservationRepository.findById(10L)).thenReturn(Optional.empty());

        assertError(ErrorCode.RESERVATION_NOT_FOUND, () -> questionnaireService.createQuestionnaire(100L, request(10L)));
        verify(questionnaireRepository, never()).save(any());
    }

    @Test
    void createQuestionnaire_fails_for_another_patients_reservation() {
        Patient loginPatient = patient(1L);
        Reservation reservation = reservation(10L, patient(2L), ReservationStatus.REQUESTED);
        mockFound(100L, loginPatient, reservation);

        assertError(ErrorCode.QUESTIONNAIRE_RESERVATION_FORBIDDEN,
                () -> questionnaireService.createQuestionnaire(100L, request(10L)));
    }

    @Test
    void createQuestionnaire_fails_when_duplicate() {
        Patient patient = patient(1L);
        Reservation reservation = reservation(10L, patient, ReservationStatus.REQUESTED);
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

    private void assertStatusFailure(ReservationStatus status, ErrorCode errorCode) {
        Patient patient = patient(1L);
        Reservation reservation = reservation(10L, patient, status);
        mockFound(100L, patient, reservation);
        assertError(errorCode, () -> questionnaireService.createQuestionnaire(100L, request(10L)));
        verify(questionnaireRepository, never()).save(any());
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

    private QuestionnaireCreateRequest request(Long reservationId) {
        return new QuestionnaireCreateRequest(
                reservationId, "복통", LocalDateTime.of(2026, 7, 29, 9, 30),
                "배가 지속적으로 아픕니다.", 7, new BigDecimal("37.5"),
                "메스꺼움", "고혈압", "혈압약", "없음", "아침부터 악화됨"
        );
    }
}
