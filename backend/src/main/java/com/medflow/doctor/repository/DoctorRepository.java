package com.medflow.doctor.repository;

import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.entity.DoctorStatus;
import com.medflow.user.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByUserId(Long userId);

    boolean existsByLicenseNumber(String licenseNumber);

    List<Doctor> findAllByStatus(DoctorStatus status);

    List<Doctor> findAllByHospitalIdAndStatusAndUserStatus(
            Long hospitalId,
            DoctorStatus status,
            UserStatus userStatus
    );

    Optional<Doctor> findByIdAndStatusAndUserStatus(
            Long doctorId,
            DoctorStatus status,
            UserStatus userStatus
    );

    List<Doctor> findAllByHospitalIdInAndStatusAndUserStatus(
            List<Long> hospitalIds,
            DoctorStatus status,
            UserStatus userStatus
    );
}
