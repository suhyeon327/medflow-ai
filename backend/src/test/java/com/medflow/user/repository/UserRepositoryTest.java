package com.medflow.user.repository;

import com.medflow.user.entity.User;
import com.medflow.user.entity.UserRole;
import com.medflow.user.entity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired UserRepository userRepository;

    private User activePatient;
    private User activeDoctor;
    private User withdrawnPatient;

    @BeforeEach
    void setUp() {
        // given
        activePatient = userRepository.save(User.create(
                "active-patient@test.com", "password", UserRole.PATIENT));
        activeDoctor = userRepository.save(User.create(
                "active-doctor@test.com", "password", UserRole.DOCTOR));
        withdrawnPatient = User.create(
                "withdrawn-patient@test.com", "password", UserRole.PATIENT);
        withdrawnPatient.withdraw();
        userRepository.saveAndFlush(withdrawnPatient);
    }

    @Test
    void 이메일로_사용자를_조회한다() {
        // when
        var result = userRepository.findByEmail("active-patient@test.com");

        // then
        assertThat(result).contains(activePatient);
    }

    @Test
    void 이메일_중복_여부를_확인한다() {
        // when
        boolean exists = userRepository.existsByEmail("active-doctor@test.com");
        boolean notExists = userRepository.existsByEmail("unknown@test.com");

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void 역할로_사용자를_페이징_조회한다() {
        // given
        PageRequest pageable = PageRequest.of(0, 10, Sort.by("email"));

        // when
        var page = userRepository.findAllByRole(UserRole.PATIENT, pageable);

        // then
        assertThat(page.getContent()).containsExactly(activePatient, withdrawnPatient);
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void 상태로_사용자를_페이징_조회한다() {
        // given
        PageRequest pageable = PageRequest.of(0, 10);

        // when
        var page = userRepository.findAllByStatus(UserStatus.WITHDRAWN, pageable);

        // then
        assertThat(page.getContent()).containsExactly(withdrawnPatient);
    }

    @Test
    void 역할과_상태를_함께_적용해_조회한다() {
        // given
        PageRequest pageable = PageRequest.of(0, 10);

        // when
        var page = userRepository.findAllByRoleAndStatus(
                UserRole.PATIENT, UserStatus.ACTIVE, pageable);

        // then
        assertThat(page.getContent()).containsExactly(activePatient);
        assertThat(page.getContent()).doesNotContain(activeDoctor, withdrawnPatient);
    }

    @Test
    void 조회_결과에_페이징이_적용된다() {
        // given
        PageRequest pageable = PageRequest.of(1, 1, Sort.by("email"));

        // when
        var page = userRepository.findAll(pageable);

        // then
        assertThat(page.getNumber()).isEqualTo(1);
        assertThat(page.getSize()).isEqualTo(1);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(3);
    }
}
