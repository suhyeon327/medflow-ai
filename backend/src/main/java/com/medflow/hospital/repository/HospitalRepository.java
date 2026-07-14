package com.medflow.hospital.repository;

import com.medflow.hospital.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;


public interface HospitalRepository extends JpaRepository<Hospital, Long> {

    // 병원 존재 여부
    boolean existsByName(String name);
}
