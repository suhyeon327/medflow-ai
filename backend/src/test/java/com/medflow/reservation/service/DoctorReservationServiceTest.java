package com.medflow.reservation.service;

import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.entity.DoctorSchedule;
import com.medflow.doctor.entity.DoctorScheduleStatus;
import com.medflow.doctor.repository.DoctorRepository;
import com.medflow.patient.entity.Gender;
import com.medflow.patient.entity.Patient;
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
    @InjectMocks DoctorReservationService service;

    @Test
    void getDoctorReservations_combinesDateAndStatusFilters() {
        Long userId = 1L;
        Doctor doctor = doctor(10L);
        LocalDate date = LocalDate.of(2026, 8, 6);
        PageRequest pageable = PageRequest.of(0, 10);
        Reservation reservation = reservation(100L);
        reservation.approve();

        when(doctorRepository.findByUserId(userId)).thenReturn(Optional.of(doctor));
        when(searchRepository.search(10L, date, ReservationStatus.APPROVED, pageable))
                .thenReturn(new PageImpl<>(List.of(reservation), pageable, 1));

        DoctorReservationPageResponse response = service.getDoctorReservations(
                userId, date, ReservationStatus.APPROVED, pageable
        );

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().getFirst().reservationStatus()).isEqualTo(ReservationStatus.APPROVED);
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

        ReservationStatusResponse response = service.updateReservationStatus(1L, 100L, ReservationStatus.COMPLETED);

        assertThat(response.status()).isEqualTo(ReservationStatus.COMPLETED);
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
        when(reservationRepository.findByIdAndDoctorScheduleDoctorId(100L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateReservationStatus(1L, 100L, ReservationStatus.APPROVED))
                .isInstanceOf(BusinessException.class);
        verify(reservationRepository).findByIdAndDoctorScheduleDoctorId(100L, 10L);
    }

    private Reservation ownedReservation(Long userId, Long doctorId, Long reservationId) {
        Doctor doctor = doctor(doctorId);
        Reservation reservation = reservation(reservationId);
        when(doctorRepository.findByUserId(userId)).thenReturn(Optional.of(doctor));
        when(reservationRepository.findByIdAndDoctorScheduleDoctorId(reservationId, doctorId))
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
}
