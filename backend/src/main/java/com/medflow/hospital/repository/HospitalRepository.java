package com.medflow.hospital.repository;

import com.medflow.hospital.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface HospitalRepository extends JpaRepository<Hospital, Long> {

    // 병원 존재 여부
    boolean existsByName(String name);

    // 병원 조회
    Optional<Hospital> findById(Long hospitalId);
}
