package com.medflow.doctor.repository;

import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.entity.DoctorSchedule;
import com.medflow.hospital.entity.Hospital;
import com.medflow.user.entity.User;
import com.medflow.user.entity.UserRole;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DoctorScheduleRepositoryTest {

    private static final LocalDate SCHEDULE_DATE = LocalDate.of(2026, 8, 10);

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private DoctorScheduleRepository doctorScheduleRepository;

    private Doctor doctor;

    @BeforeEach
    void setUp() {
        Hospital hospital = Hospital.create(
                "스케줄 테스트 병원", "서울시 강남구", "서울", "02-1234-5678"
        );
        entityManager.persist(hospital);

        User user = User.create("schedule-doctor@test.com", "password", UserRole.DOCTOR);
        entityManager.persist(user);

        doctor = Doctor.create(user, hospital, "스케줄 의사", "SCHEDULE-LICENSE");
        entityManager.persist(doctor);
        entityManager.flush();
    }

    @Test
    void existsOverlappingSchedule_returnsTrueForSameStartTime() {
        persistSchedule(LocalTime.of(9, 0), LocalTime.of(9, 30));

        boolean exists = doctorScheduleRepository.existsOverlappingSchedule(
                doctor.getId(), SCHEDULE_DATE, LocalTime.of(9, 0), LocalTime.of(9, 30)
        );

        assertThat(exists).isTrue();
    }

    @Test
    void existsOverlappingSchedule_returnsTrueForPartialOverlap() {
        persistSchedule(LocalTime.of(9, 15), LocalTime.of(9, 45));

        boolean exists = doctorScheduleRepository.existsOverlappingSchedule(
                doctor.getId(), SCHEDULE_DATE, LocalTime.of(9, 0), LocalTime.of(9, 30)
        );

        assertThat(exists).isTrue();
    }

    @Test
    void existsOverlappingSchedule_returnsTrueWhenNewScheduleIsInsideExistingSchedule() {
        persistSchedule(LocalTime.of(9, 0), LocalTime.of(10, 0));

        boolean exists = doctorScheduleRepository.existsOverlappingSchedule(
                doctor.getId(), SCHEDULE_DATE, LocalTime.of(9, 15), LocalTime.of(9, 45)
        );

        assertThat(exists).isTrue();
    }

    @Test
    void existsOverlappingSchedule_returnsTrueWhenNewScheduleContainsExistingSchedule() {
        persistSchedule(LocalTime.of(9, 15), LocalTime.of(9, 45));

        boolean exists = doctorScheduleRepository.existsOverlappingSchedule(
                doctor.getId(), SCHEDULE_DATE, LocalTime.of(9, 0), LocalTime.of(10, 0)
        );

        assertThat(exists).isTrue();
    }

    @Test
    void existsOverlappingSchedule_returnsFalseForAdjacentSchedule() {
        persistSchedule(LocalTime.of(9, 0), LocalTime.of(9, 30));

        boolean exists = doctorScheduleRepository.existsOverlappingSchedule(
                doctor.getId(), SCHEDULE_DATE, LocalTime.of(9, 30), LocalTime.of(10, 0)
        );

        assertThat(exists).isFalse();
    }

    @Test
    void saveAndFlush_rejectsSameDoctorDateAndStartTime() {
        persistSchedule(LocalTime.of(9, 0), LocalTime.of(9, 30));
        DoctorSchedule duplicatedSchedule = DoctorSchedule.create(
                doctor, SCHEDULE_DATE, LocalTime.of(9, 0), LocalTime.of(10, 0)
        );

        assertThatThrownBy(() -> doctorScheduleRepository.saveAndFlush(duplicatedSchedule))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private void persistSchedule(LocalTime startTime, LocalTime endTime) {
        doctorScheduleRepository.saveAndFlush(
                DoctorSchedule.create(doctor, SCHEDULE_DATE, startTime, endTime)
        );
    }
}
