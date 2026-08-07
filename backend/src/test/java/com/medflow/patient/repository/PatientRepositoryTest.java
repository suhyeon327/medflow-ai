package com.medflow.patient.repository;

import com.medflow.patient.entity.Gender;
import com.medflow.patient.entity.Patient;
import com.medflow.user.entity.User;
import com.medflow.user.entity.UserRole;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PatientRepositoryTest {

    @Autowired PatientRepository patientRepository;
    @Autowired EntityManager entityManager;

    private User patientUser;
    private Patient patient;

    @BeforeEach
    void setUp() {
        // given
        patientUser = User.create("patient-repository@test.com", "password", UserRole.PATIENT);
        entityManager.persist(patientUser);
        patient = Patient.create(
                patientUser, "홍길동", LocalDate.of(1999, 5, 20), Gender.MALE, "01012345678"
        );
        patientRepository.saveAndFlush(patient);
    }

    @Test
    void 사용자_ID로_환자를_조회한다() {
        // when
        var result = patientRepository.findByUserId(patientUser.getId());

        // then
        assertThat(result).contains(patient);
    }

    @Test
    void 존재하지_않는_사용자_ID로_조회하면_빈_결과를_반환한다() {
        // when
        var result = patientRepository.findByUserId(999L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 사용자_ID에_연결된_환자_존재_여부를_확인한다() {
        // when
        boolean exists = patientRepository.existsByUserId(patientUser.getId());
        boolean notExists = patientRepository.existsByUserId(999L);

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
}
