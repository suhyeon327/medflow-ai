package com.medflow.hospital.repository;

import com.medflow.doctor.entity.DoctorStatus;
import com.medflow.hospital.entity.Hospital;
import com.medflow.hospital.entity.HospitalStatus;
import com.medflow.user.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface HospitalRepository extends JpaRepository<Hospital, Long> {

    // 운영중인 병원만 조회
    Page<Hospital> findAllByStatus(HospitalStatus status, Pageable pageable);

    // 병원명 또는 지역에 검색어가 포함된 병원을 이름순으로 조회
    @Query("""
            select h from Hospital h
            where h.status = :status
              and (lower(h.name) like lower(concat('%', :keyword, '%'))
                or lower(h.address) like lower(concat('%', :keyword, '%')))
            order by h.name asc
            """)
    Page<Hospital> searchByStatusAndKeyword(
            @Param("status") HospitalStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // 병원 ID와 상태로 병원 조회
    Optional<Hospital> findByIdAndStatus(Long hospitalId, HospitalStatus status);

    // 병원 존재 여부
    boolean existsByName(String name);

    // 활성 상태에 해당하는 병원 수 조회
    Long countByStatus(HospitalStatus status);
}
