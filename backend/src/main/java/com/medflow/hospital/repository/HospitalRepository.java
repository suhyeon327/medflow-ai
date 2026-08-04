package com.medflow.hospital.repository;

import com.medflow.hospital.entity.Hospital;
import com.medflow.hospital.entity.HospitalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface HospitalRepository extends JpaRepository<Hospital, Long> {

    // 병원 존재 여부
    boolean existsByName(String name);

    // 병원 조회
    Optional<Hospital> findById(Long hospitalId);

    // 운영중인 병원만 조회
    List<Hospital> findAllByStatus(HospitalStatus status);

    @Query("""
            select h from Hospital h
            where h.status = :status
              and (lower(h.name) like lower(concat('%', :keyword, '%'))
                or lower(h.region) like lower(concat('%', :keyword, '%'))
                or lower(h.address) like lower(concat('%', :keyword, '%')))
            order by h.name asc
            """)
    List<Hospital> searchByStatusAndKeyword(
            @Param("status") HospitalStatus status,
            @Param("keyword") String keyword
    );

    // 운영중인 병원 상세 조회
    Optional<Hospital> findByIdAndStatus(Long hospitalId, HospitalStatus status);
}
