package com.medflow.reservation.repository;

import com.medflow.common.config.QuerydslConfig;
import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.entity.DoctorSchedule;
import com.medflow.hospital.entity.Hospital;
import com.medflow.hospital.entity.HospitalStatus;
import com.medflow.patient.entity.Gender;
import com.medflow.patient.entity.Patient;
import com.medflow.reservation.entity.Reservation;
import com.medflow.reservation.entity.ReservationStatus;
import com.medflow.user.entity.User;
import com.medflow.user.entity.UserRole;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({QuerydslConfig.class, DoctorReservationSearchRepository.class})
class DoctorReservationSearchRepositoryTest {

    private static final LocalDate FIRST_DATE = LocalDate.of(2026, 8, 10);
    private static final LocalDate SECOND_DATE = LocalDate.of(2026, 8, 11);

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private DoctorReservationSearchRepository searchRepository;

    private Doctor loginDoctor;
    private Reservation firstApprovedReservation;
    private Reservation secondCancelledReservation;

    @BeforeEach
    void setUp() {
        Hospital hospital = persistHospital();
        loginDoctor = persistDoctor(hospital, 1);
        Doctor anotherDoctor = persistDoctor(hospital, 2);
        Patient firstPatient = persistPatient(1);
        Patient secondPatient = persistPatient(2);

        firstApprovedReservation = persistReservation(loginDoctor, firstPatient, FIRST_DATE, LocalTime.of(9, 0));
        secondCancelledReservation = persistReservation(loginDoctor, secondPatient, SECOND_DATE, LocalTime.of(10, 0));
        secondCancelledReservation.cancel();
        persistReservation(anotherDoctor, firstPatient, FIRST_DATE, LocalTime.of(11, 0));

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void search_returnsOnlyLoggedInDoctorsReservations() {
        // given
        PageRequest pageable = PageRequest.of(0, 10);

        // when
        Page<Reservation> result = searchRepository.search(loginDoctor.getId(), null, null, pageable);

        // then
        assertThat(result.getContent())
                .extracting(Reservation::getId)
                .containsExactly(firstApprovedReservation.getId(), secondCancelledReservation.getId());
    }

    @Test
    void search_filtersByDate() {
        // given
        PageRequest pageable = PageRequest.of(0, 10);

        // when
        Page<Reservation> result = searchRepository.search(loginDoctor.getId(), FIRST_DATE, null, pageable);

        // then
        assertThat(result.getContent())
                .extracting(Reservation::getId)
                .containsExactly(firstApprovedReservation.getId());
    }

    @Test
    void search_filtersByStatus() {
        // given
        PageRequest pageable = PageRequest.of(0, 10);

        // when
        Page<Reservation> result = searchRepository.search(
                loginDoctor.getId(), null, ReservationStatus.CANCELLED, pageable
        );

        // then
        assertThat(result.getContent())
                .extracting(Reservation::getId)
                .containsExactly(secondCancelledReservation.getId());
    }

    @Test
    void search_filtersByDateAndStatus() {
        // given
        PageRequest pageable = PageRequest.of(0, 10);

        // when
        Page<Reservation> result = searchRepository.search(
                loginDoctor.getId(), SECOND_DATE, ReservationStatus.CANCELLED, pageable
        );

        // then
        assertThat(result.getContent())
                .extracting(Reservation::getId)
                .containsExactly(secondCancelledReservation.getId());
    }

    @Test
    void search_appliesPaginationAndSort() {
        // given
        PageRequest pageable = PageRequest.of(
                1, 1, Sort.by(Sort.Order.asc("reservationDate"), Sort.Order.asc("startTime"))
        );

        // when
        Page<Reservation> result = searchRepository.search(loginDoctor.getId(), null, null, pageable);

        // then
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(Reservation::getId)
                .containsExactly(secondCancelledReservation.getId());
    }

    private Hospital persistHospital() {
        Hospital hospital = Hospital.create(
                "테스트 병원", "서울시 강남구", "서울", "02-1234-5678", HospitalStatus.ACTIVE
        );
        entityManager.persist(hospital);
        return hospital;
    }

    private Doctor persistDoctor(Hospital hospital, int sequence) {
        User user = User.create(
                "doctor" + sequence + "@test.com", "password", UserRole.DOCTOR
        );
        entityManager.persist(user);
        Doctor doctor = Doctor.create(user, hospital, "의사" + sequence, "LICENSE-" + sequence);
        entityManager.persist(doctor);
        return doctor;
    }

    private Patient persistPatient(int sequence) {
        User user = User.create(
                "patient" + sequence + "@test.com", "password", UserRole.PATIENT
        );
        entityManager.persist(user);
        Patient patient = Patient.create(
                user, "환자" + sequence, LocalDate.of(1990, 1, sequence), Gender.MALE, "0100000000" + sequence
        );
        entityManager.persist(patient);
        return patient;
    }

    private Reservation persistReservation(
            Doctor doctor,
            Patient patient,
            LocalDate date,
            LocalTime startTime
    ) {
        DoctorSchedule schedule = DoctorSchedule.create(
                doctor, date, startTime, startTime.plusMinutes(30)
        );
        entityManager.persist(schedule);
        Reservation reservation = Reservation.create(patient, schedule);
        entityManager.persist(reservation);
        return reservation;
    }
}
