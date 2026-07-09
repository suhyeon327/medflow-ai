package com.medflow.patient.repository;

import com.medflow.patient.entity.Patient;
import com.medflow.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    // User를 통해 Patient 조회
    Optional<Patient> findByUser(User user);

    // User의 환자 프로필 존재 여부 확인
    boolean existsByUser(User user);
}
