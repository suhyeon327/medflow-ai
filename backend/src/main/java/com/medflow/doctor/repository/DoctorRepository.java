package com.medflow.doctor.repository;

import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.entity.DoctorStatus;
import com.medflow.hospital.dto.projection.HospitalDoctorProjection;
import com.medflow.user.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByUserId(Long userId);

    boolean existsByLicenseNumber(String licenseNumber);

    List<Doctor> findAllByStatus(DoctorStatus status);

    Optional<Doctor> findByIdAndStatusAndUserStatus(
            Long doctorId,
            DoctorStatus status,
            UserStatus userStatus
    );

    // 여러 병원에 소속된 의사 목록 조회
    List<Doctor> findAllByHospitalIdInAndStatusAndUserStatus(
            List<Long> hospitalIds,
            DoctorStatus status,
            UserStatus userStatus
    );

    // 특정 병원에 소속된 의사 목록 조회
    List<Doctor> findAllByHospitalIdAndStatusAndUserStatus(
            Long hospitalId,
            DoctorStatus status,
            UserStatus userStatus
    );

    // 활성 상태에 해당하는 의사 수 조회
    long countByStatusAndUserStatus(
            DoctorStatus status,
            UserStatus userStatus
    );

    // 병원 목록 조회에 필요한 hospitalId와 specialty만 조회
    @Query("""
    select
        d.hospital.id as hospitalId,
        d.specialty as specialty
    from Doctor d
    join d.user u
    where d.hospital.id in :hospitalIds
      and d.status = :doctorStatus
      and u.status = :userStatus
    """)
    List<HospitalDoctorProjection> findHospitalDoctorProjections(
            @Param("hospitalIds") List<Long> hospitalIds,
            @Param("doctorStatus") DoctorStatus doctorStatus,
            @Param("userStatus") UserStatus userStatus
    );
}
