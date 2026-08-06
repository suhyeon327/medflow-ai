package com.medflow.reservation.service;

import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.entity.DoctorSchedule;
import com.medflow.doctor.entity.DoctorScheduleStatus;
import com.medflow.doctor.repository.DoctorRepository;
import com.medflow.patient.entity.Gender;
import com.medflow.patient.entity.Patient;
import com.medflow.questionnaire.entity.Questionnaire;
import com.medflow.questionnaire.repository.QuestionnaireRepository;
import com.medflow.reservation.dto.response.DoctorReservationPageResponse;
import com.medflow.reservation.dto.response.ReservationStatusResponse;
import com.medflow.reservation.entity.Reservation;
import com.medflow.reservation.entity.ReservationStatus;
import com.medflow.reservation.repository.DoctorReservationSearchRepository;
import com.medflow.reservation.repository.ReservationRepository;
import com.medflow.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorReservationServiceTest {

    @Mock ReservationRepository reservationRepository;
    @Mock DoctorRepository doctorRepository;
    @Mock DoctorReservationSearchRepository searchRepository;
    @Mock QuestionnaireRepository questionnaireRepository;
    @Mock Clock clock;
    @InjectMocks DoctorReservationService service;

    @Test
    void getDoctorReservations_combinesDateAndStatusFilters() {
        Long userId = 1L;
        Doctor doctor = doctor(10L);
        LocalDate date = LocalDate.of(2026, 8, 6);
        PageRequest pageable = PageRequest.of(0, 10);
        Reservation reservation = reservation(100L);
        reservation.approve();
        Questionnaire questionnaire = questionnaire(200L, reservation);

        when(doctorRepository.findByUserId(userId)).thenReturn(Optional.of(doctor));
        when(searchRepository.search(10L, date, ReservationStatus.APPROVED, pageable))
                .thenReturn(new PageImpl<>(List.of(reservation), pageable, 1));
        when(questionnaireRepository.findAllByReservationIdIn(List.of(100L)))
                .thenReturn(List.of(questionnaire));

        DoctorReservationPageResponse response = service.getDoctorReservations(
                userId, date, ReservationStatus.APPROVED, pageable
        );

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().getFirst().reservationStatus()).isEqualTo(ReservationStatus.APPROVED);
        assertThat(response.content().getFirst().questionnaireId()).isEqualTo(200L);
    }

    @Test
    void getDoctorReservations_returnsNullQuestionnaireId_whenQuestionnaireDoesNotExist() {
        Doctor doctor = doctor(10L);
        PageRequest pageable = PageRequest.of(0, 10);
        Reservation reservation = reservation(100L);
        when(doctorRepository.findByUserId(1L)).thenReturn(Optional.of(doctor));
        when(searchRepository.search(10L, null, null, pageable))
                .thenReturn(new PageImpl<>(List.of(reservation), pageable, 1));
        when(questionnaireRepository.findAllByReservationIdIn(List.of(100L))).thenReturn(List.of());

        DoctorReservationPageResponse response = service.getDoctorReservations(1L, null, null, pageable);

        assertThat(response.content().getFirst().questionnaireId()).isNull();
    }

    @Test
    void updateReservationStatus_approvesPendingReservation() {
        Reservation reservation = ownedReservation(1L, 10L, 100L);

        ReservationStatusResponse response = service.updateReservationStatus(1L, 100L, ReservationStatus.APPROVED);

        assertThat(response.status()).isEqualTo(ReservationStatus.APPROVED);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.APPROVED);
    }

    @Test
    void updateReservationStatus_rejectsPendingReservationAndReleasesSchedule() {
        Reservation reservation = ownedReservation(1L, 10L, 100L);
        reservation.getDoctorSchedule().reserve();

        ReservationStatusResponse response = service.updateReservationStatus(1L, 100L, ReservationStatus.REJECTED);

        assertThat(response.status()).isEqualTo(ReservationStatus.REJECTED);
        assertThat(reservation.getDoctorSchedule().getStatus()).isEqualTo(DoctorScheduleStatus.AVAILABLE);
    }

    @Test
    void updateReservationStatus_completesApprovedReservation() {
        Reservation reservation = ownedReservation(1L, 10L, 100L);
        reservation.approve();
        setCurrentTime("2026-08-06T01:00:00Z");

        ReservationStatusResponse response = service.updateReservationStatus(1L, 100L, ReservationStatus.COMPLETED);

        assertThat(response.status()).isEqualTo(ReservationStatus.COMPLETED);
    }

    @Test
    void updateReservationStatus_rejectsCompletionBeforeEndTime() {
        Reservation reservation = ownedReservation(1L, 10L, 100L);
        reservation.approve();
        setCurrentTime("2026-08-05T23:30:00Z");

        assertThatThrownBy(() -> service.updateReservationStatus(1L, 100L, ReservationStatus.COMPLETED))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESERVATION_NOT_ENDED);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.APPROVED);
    }

    @Test
    void updateReservationStatus_rejectsUnsupportedTargetStatus() {
        ownedReservation(1L, 10L, 100L);

        assertThatThrownBy(() -> service.updateReservationStatus(1L, 100L, ReservationStatus.CANCELLED))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_STATUS_CHANGE);
    }

    @Test
    void updateReservationStatus_rejectsReservationOwnedByAnotherDoctor() {
        Doctor doctor = doctor(10L);
        when(doctorRepository.findByUserId(1L)).thenReturn(Optional.of(doctor));
        when(reservationRepository.findDoctorReservationForUpdate(100L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateReservationStatus(1L, 100L, ReservationStatus.APPROVED))
                .isInstanceOf(BusinessException.class);
        verify(reservationRepository).findDoctorReservationForUpdate(100L, 10L);
    }

    @Test
    void updateReservationStatus_rejectsDuplicateCompletionAfterSchedulerCompletion() {
        Reservation reservation = ownedReservation(1L, 10L, 100L);
        reservation.approve();
        Clock fixedClock = Clock.fixed(Instant.parse("2026-08-06T01:00:00Z"), ZoneId.of("Asia/Seoul"));
        when(reservationRepository.findCompletionTargets(
                ReservationStatus.APPROVED,
                LocalDate.of(2026, 8, 6),
                LocalTime.of(10, 0),
                PageRequest.of(0, 100)
        )).thenReturn(List.of(reservation));

        new ReservationCompletionService(reservationRepository, fixedClock).completeEndedReservations();
        setCurrentTime("2026-08-06T01:00:00Z");

        assertThatThrownBy(() -> service.updateReservationStatus(1L, 100L, ReservationStatus.COMPLETED))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_STATUS_CHANGE);
    }

    private Reservation ownedReservation(Long userId, Long doctorId, Long reservationId) {
        Doctor doctor = doctor(doctorId);
        Reservation reservation = reservation(reservationId);
        when(doctorRepository.findByUserId(userId)).thenReturn(Optional.of(doctor));
        when(reservationRepository.findDoctorReservationForUpdate(reservationId, doctorId))
                .thenReturn(Optional.of(reservation));
        return reservation;
    }

    private Doctor doctor(Long id) {
        Doctor doctor = mock(Doctor.class);
        when(doctor.getId()).thenReturn(id);
        return doctor;
    }

    private Reservation reservation(Long id) {
        Patient patient = Patient.create(mock(User.class), "환자", LocalDate.of(2000, 1, 1), Gender.MALE, "01012345678");
        DoctorSchedule schedule = DoctorSchedule.create(mock(Doctor.class), LocalDate.of(2026, 8, 6), LocalTime.of(9, 0), LocalTime.of(9, 30));
        Reservation reservation = Reservation.create(patient, schedule);
        ReflectionTestUtils.setField(reservation, "id", id);
        return reservation;
    }

    private void setCurrentTime(String instant) {
        when(clock.instant()).thenReturn(Instant.parse(instant));
        when(clock.getZone()).thenReturn(ZoneId.of("Asia/Seoul"));
    }

    private Questionnaire questionnaire(Long id, Reservation reservation) {
        Questionnaire questionnaire = Questionnaire.create(
                reservation,
                "기침",
                LocalDateTime.of(2026, 8, 5, 9, 0),
                "기침 증상",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        ReflectionTestUtils.setField(questionnaire, "id", id);
        return questionnaire;
    }
}
