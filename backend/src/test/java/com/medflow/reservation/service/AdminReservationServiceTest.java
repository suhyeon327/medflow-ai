package com.medflow.reservation.service;

import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.entity.DoctorSchedule;
import com.medflow.hospital.entity.Hospital;
import com.medflow.patient.entity.Gender;
import com.medflow.patient.entity.Patient;
import com.medflow.reservation.dto.response.AdminReservationPageResponse;
import com.medflow.reservation.entity.Reservation;
import com.medflow.reservation.entity.ReservationStatus;
import com.medflow.reservation.repository.AdminReservationSearchRepository;
import com.medflow.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminReservationServiceTest {

    @Mock
    private AdminReservationSearchRepository adminReservationSearchRepository;

    @InjectMocks
    private AdminReservationService adminReservationService;

    @Test
    void searchReservations_success() {
        AdminReservationPageResponse response = search(null, null, null, null, null, PageRequest.of(0, 20), List.of(reservation()));
        assertThat(response.content()).hasSize(1);
        assertThat(response.totalElements()).isEqualTo(1);
    }

    @Test
    void searchReservations_byHospital() {
        search(1L, null, null, null, null, PageRequest.of(0, 20), List.of());
        verify(adminReservationSearchRepository).search(eq(1L), eq(null), eq(null), eq(null), eq(null), any());
    }

    @Test
    void searchReservations_byDoctor() {
        search(null, 2L, null, null, null, PageRequest.of(0, 20), List.of());
        verify(adminReservationSearchRepository).search(eq(null), eq(2L), eq(null), eq(null), eq(null), any());
    }

    @Test
    void searchReservations_byPatient() {
        search(null, null, 3L, null, null, PageRequest.of(0, 20), List.of());
        verify(adminReservationSearchRepository).search(eq(null), eq(null), eq(3L), eq(null), eq(null), any());
    }

    @Test
    void searchReservations_byDateAndStatus() {
        LocalDate date = LocalDate.of(2026, 7, 30);
        search(null, null, null, date, ReservationStatus.APPROVED, PageRequest.of(0, 20), List.of());
        verify(adminReservationSearchRepository).search(eq(null), eq(null), eq(null), eq(date), eq(ReservationStatus.APPROVED), any());
    }

    @Test
    void searchReservations_byCombinedConditions() {
        LocalDate date = LocalDate.of(2026, 7, 30);
        search(1L, 2L, 3L, date, ReservationStatus.APPROVED, PageRequest.of(0, 20), List.of());
        verify(adminReservationSearchRepository).search(eq(1L), eq(2L), eq(3L), eq(date), eq(ReservationStatus.APPROVED), any());
    }

    @Test
    void searchReservations_returnsEmptyPage() {
        assertThat(search(null, null, null, null, null, PageRequest.of(0, 20), List.of()).content()).isEmpty();
    }

    @Test
    void searchReservations_appliesPaging() {
        AdminReservationPageResponse response = search(null, null, null, null, null, PageRequest.of(1, 1), List.of(reservation()));
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(1);
    }

    @Test
    void searchReservations_appliesDefaultSort() {
        search(null, null, null, null, null, PageRequest.of(0, 20), List.of());
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(adminReservationSearchRepository).search(eq(null), eq(null), eq(null), eq(null), eq(null), captor.capture());
        assertThat(captor.getValue().getSort()).containsExactly(
                Sort.Order.desc("reservationDate"),
                Sort.Order.asc("startTime")
        );
    }

    private AdminReservationPageResponse search(Long hospitalId, Long doctorId, Long patientId, LocalDate date,
                                                 ReservationStatus status, Pageable pageable, List<Reservation> content) {
        when(adminReservationSearchRepository.search(eq(hospitalId), eq(doctorId), eq(patientId), eq(date), eq(status), any()))
                .thenAnswer(invocation -> new PageImpl<>(content, invocation.getArgument(5), content.size()));
        return adminReservationService.searchReservations(hospitalId, doctorId, patientId, date, status, pageable);
    }

    private Reservation reservation() {
        Hospital hospital = mock(Hospital.class);
        Doctor doctor = mock(Doctor.class);
        Patient patient = Patient.create(mock(User.class), "환자", LocalDate.of(2000, 1, 1), Gender.MALE, "01012345678");
        DoctorSchedule schedule = DoctorSchedule.create(doctor, LocalDate.of(2026, 7, 30), LocalTime.of(9, 0), LocalTime.of(9, 30));
        Reservation reservation = Reservation.create(patient, schedule);
        when(hospital.getId()).thenReturn(1L);
        when(hospital.getName()).thenReturn("병원");
        when(doctor.getId()).thenReturn(2L);
        when(doctor.getName()).thenReturn("의사");
        when(doctor.getHospital()).thenReturn(hospital);
        ReflectionTestUtils.setField(patient, "id", 3L);
        ReflectionTestUtils.setField(reservation, "id", 4L);
        ReflectionTestUtils.setField(reservation, "createdAt", LocalDateTime.of(2026, 7, 1, 9, 0));
        return reservation;
    }
}
