package com.medflow.reservation.service;

import com.medflow.common.exception.BusinessException;
import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.entity.DoctorSchedule;
import com.medflow.doctor.repository.DoctorRepository;
import com.medflow.patient.entity.Gender;
import com.medflow.patient.entity.Patient;
import com.medflow.reservation.dto.response.DoctorReservationResponse;
import com.medflow.reservation.entity.Reservation;
import com.medflow.reservation.entity.ReservationStatus;
import com.medflow.reservation.repository.ReservationRepository;
import com.medflow.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorReservationService doctorReservationService;

    @Test
    void getDoctorReservations_success() {

        Long userId = 1L;
        Long doctorId = 10L;
        Doctor doctor = doctor(doctorId);
        Reservation reservation = reservation(1L, "Patient", LocalDate.of(2026, 8, 1), LocalTime.of(9, 0));

        when(doctorRepository.findByUserId(userId)).thenReturn(Optional.of(doctor));
        when(reservationRepository.findAllByDoctorScheduleDoctorIdOrderByDoctorScheduleDateAscDoctorScheduleStartTimeAsc(doctorId))
                .thenReturn(List.of(reservation));

        List<DoctorReservationResponse> result = doctorReservationService.getDoctorReservations(userId);

        assertThat(result).singleElement().satisfies(response -> {
            assertThat(response.reservationId()).isEqualTo(1L);
            assertThat(response.patientName()).isEqualTo("Patient");
            assertThat(response.reservationDate()).isEqualTo(LocalDate.of(2026, 8, 1));
            assertThat(response.startTime()).isEqualTo(LocalTime.of(9, 0));
        });
    }

    @Test
    void getDoctorReservations_doesNotQueryAnotherDoctorsReservations() {

        Long userId = 1L;
        Long doctorId = 10L;
        Doctor doctor = doctor(doctorId);

        when(doctorRepository.findByUserId(userId)).thenReturn(Optional.of(doctor));
        when(reservationRepository.findAllByDoctorScheduleDoctorIdOrderByDoctorScheduleDateAscDoctorScheduleStartTimeAsc(doctorId))
                .thenReturn(List.of());

        assertThat(doctorReservationService.getDoctorReservations(userId)).isEmpty();
        verify(reservationRepository)
                .findAllByDoctorScheduleDoctorIdOrderByDoctorScheduleDateAscDoctorScheduleStartTimeAsc(doctorId);
    }

    @Test
    void getDoctorReservations_returnsEmptyList_whenNoReservations() {

        Long userId = 1L;
        Long doctorId = 10L;
        Doctor doctor = doctor(doctorId);

        when(doctorRepository.findByUserId(userId)).thenReturn(Optional.of(doctor));
        when(reservationRepository.findAllByDoctorScheduleDoctorIdOrderByDoctorScheduleDateAscDoctorScheduleStartTimeAsc(doctorId))
                .thenReturn(List.of());

        assertThat(doctorReservationService.getDoctorReservations(userId)).isEmpty();
    }

    @Test
    void getDoctorReservations_returnsReservationsInDateAndTimeOrder() {

        Long userId = 1L;
        Long doctorId = 10L;
        Doctor doctor = doctor(doctorId);
        Reservation first = reservation(1L, "First", LocalDate.of(2026, 8, 1), LocalTime.of(9, 0));
        Reservation second = reservation(2L, "Second", LocalDate.of(2026, 8, 1), LocalTime.of(10, 0));
        Reservation third = reservation(3L, "Third", LocalDate.of(2026, 8, 2), LocalTime.of(9, 0));

        when(doctorRepository.findByUserId(userId)).thenReturn(Optional.of(doctor));
        when(reservationRepository.findAllByDoctorScheduleDoctorIdOrderByDoctorScheduleDateAscDoctorScheduleStartTimeAsc(doctorId))
                .thenReturn(List.of(first, second, third));

        List<DoctorReservationResponse> result = doctorReservationService.getDoctorReservations(userId);

        assertThat(result).extracting(DoctorReservationResponse::reservationId)
                .containsExactly(1L, 2L, 3L);
    }

    @Test
    void getTodayDoctorReservations_success() {

        Long userId = 1L;
        Long doctorId = 10L;
        LocalDate today = LocalDate.now();
        Doctor doctor = doctor(doctorId);
        Reservation reservation = reservation(1L, "Patient", today, LocalTime.of(9, 0), ReservationStatus.CONFIRMED);

        when(doctorRepository.findByUserId(userId)).thenReturn(Optional.of(doctor));
        when(reservationRepository.findTodayReservationsByDoctorId(
                doctorId, today, List.of(ReservationStatus.REQUESTED, ReservationStatus.CONFIRMED)))
                .thenReturn(List.of(reservation));

        List<DoctorReservationResponse> result = doctorReservationService.getTodayDoctorReservations(userId);

        assertThat(result).singleElement().satisfies(response -> {
            assertThat(response.reservationId()).isEqualTo(1L);
            assertThat(response.reservationDate()).isEqualTo(today);
            assertThat(response.reservationStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        });
    }

    @Test
    void getTodayDoctorReservations_doesNotIncludeAnotherDoctorsReservations() {

        Long userId = 1L;
        Long doctorId = 10L;
        LocalDate today = LocalDate.now();
        Doctor doctor = doctor(doctorId);

        when(doctorRepository.findByUserId(userId)).thenReturn(Optional.of(doctor));
        when(reservationRepository.findTodayReservationsByDoctorId(
                doctorId, today, List.of(ReservationStatus.REQUESTED, ReservationStatus.CONFIRMED)))
                .thenReturn(List.of());

        assertThat(doctorReservationService.getTodayDoctorReservations(userId)).isEmpty();
        verify(reservationRepository).findTodayReservationsByDoctorId(
                doctorId, today, List.of(ReservationStatus.REQUESTED, ReservationStatus.CONFIRMED));
    }

    @Test
    void getTodayDoctorReservations_excludesYesterdayAndTomorrowReservations() {

        Long userId = 1L;
        Long doctorId = 10L;
        LocalDate today = LocalDate.now();
        Doctor doctor = doctor(doctorId);
        Reservation todayReservation = reservation(2L, "Today", today, LocalTime.of(9, 0), ReservationStatus.REQUESTED);

        when(doctorRepository.findByUserId(userId)).thenReturn(Optional.of(doctor));
        when(reservationRepository.findTodayReservationsByDoctorId(
                doctorId, today, List.of(ReservationStatus.REQUESTED, ReservationStatus.CONFIRMED)))
                .thenReturn(List.of(todayReservation));

        List<DoctorReservationResponse> result = doctorReservationService.getTodayDoctorReservations(userId);

        assertThat(result).extracting(DoctorReservationResponse::reservationDate)
                .containsOnly(today);
        verify(reservationRepository).findTodayReservationsByDoctorId(
                doctorId, today, List.of(ReservationStatus.REQUESTED, ReservationStatus.CONFIRMED));
    }

    @Test
    void getTodayDoctorReservations_returnsReservationsInStartTimeOrder() {

        Long userId = 1L;
        Long doctorId = 10L;
        LocalDate today = LocalDate.now();
        Doctor doctor = doctor(doctorId);
        Reservation first = reservation(1L, "First", today, LocalTime.of(9, 0), ReservationStatus.REQUESTED);
        Reservation second = reservation(2L, "Second", today, LocalTime.of(10, 0), ReservationStatus.CONFIRMED);

        when(doctorRepository.findByUserId(userId)).thenReturn(Optional.of(doctor));
        when(reservationRepository.findTodayReservationsByDoctorId(
                doctorId, today, List.of(ReservationStatus.REQUESTED, ReservationStatus.CONFIRMED)))
                .thenReturn(List.of(first, second));

        assertThat(doctorReservationService.getTodayDoctorReservations(userId))
                .extracting(DoctorReservationResponse::startTime)
                .containsExactly(LocalTime.of(9, 0), LocalTime.of(10, 0));
    }

    @Test
    void getTodayDoctorReservations_excludesCancelledReservations() {

        assertTodayReservationsExclude(ReservationStatus.CANCELLED);
    }

    @Test
    void getTodayDoctorReservations_excludesCompletedReservations() {

        assertTodayReservationsExclude(ReservationStatus.COMPLETED);
    }

    @Test
    void getTodayDoctorReservations_returnsEmptyList_whenNoReservations() {

        Long userId = 1L;
        Long doctorId = 10L;
        LocalDate today = LocalDate.now();
        Doctor doctor = doctor(doctorId);

        when(doctorRepository.findByUserId(userId)).thenReturn(Optional.of(doctor));
        when(reservationRepository.findTodayReservationsByDoctorId(
                doctorId, today, List.of(ReservationStatus.REQUESTED, ReservationStatus.CONFIRMED)))
                .thenReturn(List.of());

        assertThat(doctorReservationService.getTodayDoctorReservations(userId)).isEmpty();
    }

    @Test
    void getTodayDoctorReservations_throwsException_whenUserIsNotDoctor() {

        when(doctorRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorReservationService.getTodayDoctorReservations(1L))
                .isInstanceOf(BusinessException.class);
    }

    private void assertTodayReservationsExclude(ReservationStatus excludedStatus) {

        Long userId = 1L;
        Long doctorId = 10L;
        LocalDate today = LocalDate.now();
        Doctor doctor = doctor(doctorId);

        when(doctorRepository.findByUserId(userId)).thenReturn(Optional.of(doctor));
        when(reservationRepository.findTodayReservationsByDoctorId(
                doctorId, today, List.of(ReservationStatus.REQUESTED, ReservationStatus.CONFIRMED)))
                .thenReturn(List.of());

        List<DoctorReservationResponse> result = doctorReservationService.getTodayDoctorReservations(userId);

        assertThat(result).noneMatch(response -> response.reservationStatus() == excludedStatus);
        verify(reservationRepository).findTodayReservationsByDoctorId(
                doctorId, today, List.of(ReservationStatus.REQUESTED, ReservationStatus.CONFIRMED));
    }

    private Doctor doctor(Long doctorId) {
        Doctor doctor = mock(Doctor.class);
        when(doctor.getId()).thenReturn(doctorId);
        return doctor;
    }

    private Reservation reservation(Long reservationId, String patientName, LocalDate date, LocalTime startTime) {
        Patient patient = Patient.create(mock(User.class), patientName, LocalDate.of(2000, 1, 1), Gender.MALE, "01012345678");
        DoctorSchedule schedule = DoctorSchedule.create(mock(Doctor.class), date, startTime, startTime.plusMinutes(30));
        Reservation reservation = Reservation.create(patient, schedule);
        ReflectionTestUtils.setField(reservation, "id", reservationId);
        return reservation;
    }

    private Reservation reservation(
            Long reservationId,
            String patientName,
            LocalDate date,
            LocalTime startTime,
            ReservationStatus status
    ) {
        Reservation reservation = reservation(reservationId, patientName, date, startTime);
        ReflectionTestUtils.setField(reservation, "status", status);
        return reservation;
    }
}
