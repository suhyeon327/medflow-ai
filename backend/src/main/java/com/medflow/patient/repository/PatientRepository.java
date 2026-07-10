package com.medflow.patient.repository;

import com.medflow.patient.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    // Patient 조회
    Optional<Patient> findByUserId(Long userId);

    // 환자 존재 여부
    boolean existsByUserId(Long userId);
}
