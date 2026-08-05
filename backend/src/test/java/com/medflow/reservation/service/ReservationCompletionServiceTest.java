package com.medflow.reservation.service;

import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.entity.DoctorSchedule;
import com.medflow.patient.entity.Gender;
import com.medflow.patient.entity.Patient;
import com.medflow.reservation.entity.Reservation;
import com.medflow.reservation.entity.ReservationStatus;
import com.medflow.reservation.repository.ReservationRepository;
import com.medflow.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationCompletionServiceTest {

    private static final ZoneId SEOUL_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final Clock NOW = Clock.fixed(Instant.parse("2026-08-06T01:00:00Z"), SEOUL_ZONE_ID);

    @Mock
    private ReservationRepository reservationRepository;

    @Test
    void completeEndedReservations_completesPastApprovedReservation() {
        Reservation reservation = reservation(LocalDate.of(2026, 8, 5), LocalTime.of(9, 30), ReservationStatus.APPROVED);
        ReservationCompletionService service = serviceWithTargets(List.of(reservation));

        int completedCount = service.completeEndedReservations();

        assertThat(completedCount).isEqualTo(1);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.COMPLETED);
    }

    @Test
    void completeEndedReservations_doesNotChangeFutureApprovedReservation() {
        Reservation reservation = reservation(LocalDate.of(2026, 8, 7), LocalTime.of(9, 30), ReservationStatus.APPROVED);
        ReservationCompletionService service = serviceWithTargets(List.of());

        assertThat(service.completeEndedReservations()).isZero();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.APPROVED);
    }

    @ParameterizedTest
    @EnumSource(value = ReservationStatus.class, names = {"PENDING", "REJECTED", "CANCELLED", "COMPLETED"})
    void completeEndedReservations_doesNotChangeNonApprovedReservation(ReservationStatus status) {
        Reservation reservation = reservation(LocalDate.of(2026, 8, 5), LocalTime.of(9, 30), status);
        ReservationCompletionService service = serviceWithTargets(List.of());

        assertThat(service.completeEndedReservations()).isZero();
        assertThat(reservation.getStatus()).isEqualTo(status);
    }

    @Test
    void completeEndedReservations_doesNotChangeTodaysReservationBeforeEndTime() {
        Reservation reservation = reservation(LocalDate.of(2026, 8, 6), LocalTime.of(10, 30), ReservationStatus.APPROVED);
        ReservationCompletionService service = serviceWithTargets(List.of());

        assertThat(service.completeEndedReservations()).isZero();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.APPROVED);
    }

    @Test
    void completeEndedReservations_completesTodaysReservationAfterEndTime() {
        Reservation reservation = reservation(LocalDate.of(2026, 8, 6), LocalTime.of(9, 30), ReservationStatus.APPROVED);
        ReservationCompletionService service = serviceWithTargets(List.of(reservation));

        assertThat(service.completeEndedReservations()).isEqualTo(1);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.COMPLETED);
        verify(reservationRepository).findCompletionTargets(
                ReservationStatus.APPROVED,
                LocalDate.of(2026, 8, 6),
                LocalTime.of(10, 0),
                PageRequest.of(0, 100)
        );
    }

    private ReservationCompletionService serviceWithTargets(List<Reservation> targets) {
        when(reservationRepository.findCompletionTargets(
                ReservationStatus.APPROVED,
                LocalDate.of(2026, 8, 6),
                LocalTime.of(10, 0),
                PageRequest.of(0, 100)
        )).thenReturn(targets);
        return new ReservationCompletionService(reservationRepository, NOW);
    }

    private Reservation reservation(LocalDate date, LocalTime endTime, ReservationStatus status) {
        Patient patient = Patient.create(mock(User.class), "환자", LocalDate.of(2000, 1, 1), Gender.MALE, "01012345678");
        DoctorSchedule schedule = DoctorSchedule.create(mock(Doctor.class), date, endTime.minusMinutes(30), endTime);
        Reservation reservation = Reservation.create(patient, schedule);
        ReflectionTestUtils.setField(reservation, "status", status);
        return reservation;
    }
}
