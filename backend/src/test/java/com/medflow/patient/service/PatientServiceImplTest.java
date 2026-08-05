package com.medflow.patient.service;

import com.medflow.patient.dto.PatientRequest;
import com.medflow.patient.dto.PatientResponse;
import com.medflow.patient.entity.Gender;
import com.medflow.patient.entity.Patient;
import com.medflow.patient.repository.PatientRepository;
import com.medflow.user.entity.User;
import com.medflow.user.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private PatientRepository patientRepository;

    private PatientServiceImpl patientService;

    @BeforeEach
    void setUp() {
        patientService = new PatientServiceImpl(patientRepository);
    }

    @Test
    void 환자_프로필을_조회한다() {
        Long userId = 1L;
        Patient patient = Patient.create(
                User.create("patient@example.com", "password", UserRole.PATIENT),
                "홍길동",
                LocalDate.of(1999, 5, 20),
                Gender.MALE,
                "01012345678"
        );
        when(patientRepository.findByUserId(userId)).thenReturn(Optional.of(patient));

        PatientResponse response = patientService.getPatientProfile(userId);

        assertThat(response.name()).isEqualTo("홍길동");
        assertThat(response.birth()).isEqualTo(LocalDate.of(1999, 5, 20));
        assertThat(response.gender()).isEqualTo(Gender.MALE);
        assertThat(response.phone()).isEqualTo("01012345678");
    }

    @Test
    void 환자_프로필을_수정한다() {
        Long userId = 1L;
        Patient patient = Patient.create(
                User.create("patient@example.com", "password", UserRole.PATIENT),
                "홍길동",
                LocalDate.of(1999, 5, 20),
                Gender.MALE,
                "01012345678"
        );
        PatientRequest request = new PatientRequest(
                "김환자",
                LocalDate.of(2000, 1, 1),
                Gender.FEMALE,
                "01087654321"
        );
        when(patientRepository.findByUserId(userId)).thenReturn(Optional.of(patient));

        PatientResponse response = patientService.updatePatientProfile(userId, request);

        assertThat(response.name()).isEqualTo("김환자");
        assertThat(response.birth()).isEqualTo(LocalDate.of(2000, 1, 1));
        assertThat(response.gender()).isEqualTo(Gender.FEMALE);
        assertThat(response.phone()).isEqualTo("01087654321");
    }
}
