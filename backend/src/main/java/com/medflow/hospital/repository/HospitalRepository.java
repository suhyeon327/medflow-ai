package com.medflow.hospital.repository;

import com.medflow.hospital.entity.Hospital;
import com.medflow.hospital.entity.HospitalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface HospitalRepository extends JpaRepository<Hospital, Long> {

    // 병원 존재 여부
    boolean existsByName(String name);

    // 병원 조회
    Optional<Hospital> findById(Long hospitalId);

    // 운영중인 병원만 조회
    List<Hospital> findAllByStatus(HospitalStatus status);
}
