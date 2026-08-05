package com.medflow.doctor.repository;

import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.entity.DoctorSchedule;
import com.medflow.doctor.entity.DoctorScheduleStatus;
import com.medflow.doctor.entity.DoctorStatus;
import com.medflow.user.entity.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DoctorRepository {

    private final DoctorJpaRepository doctorJpaRepository;
    private final DoctorScheduleJpaRepository doctorScheduleJpaRepository;

    public Doctor save(Doctor doctor) {
        return doctorJpaRepository.save(doctor);
    }

    public Optional<Doctor> findById(Long doctorId) {
        return doctorJpaRepository.findById(doctorId);
    }

    public Optional<Doctor> findByUserId(Long userId) {
        return doctorJpaRepository.findByUserId(userId);
    }

    public boolean existsByLicenseNumber(String licenseNumber) {
        return doctorJpaRepository.existsByLicenseNumber(licenseNumber);
    }

    public List<Doctor> findAllByHospitalIdAndStatusAndUserStatus(
            Long hospitalId,
            DoctorStatus status,
            UserStatus userStatus
    ) {
        return doctorJpaRepository.findAllByHospitalIdAndStatusAndUserStatus(
                hospitalId,
                status,
                userStatus
        );
    }

    public Optional<Doctor> findByIdAndStatusAndUserStatus(
            Long doctorId,
            DoctorStatus status,
            UserStatus userStatus
    ) {
        return doctorJpaRepository.findByIdAndStatusAndUserStatus(doctorId, status, userStatus);
    }

    public boolean existsSchedule(Long doctorId, LocalDate date, LocalTime startTime) {
        return doctorScheduleJpaRepository.existsByDoctorIdAndDateAndStartTime(
                doctorId,
                date,
                startTime
        );
    }

    public DoctorSchedule saveSchedule(DoctorSchedule doctorSchedule) {
        return doctorScheduleJpaRepository.save(doctorSchedule);
    }

    public Optional<DoctorSchedule> findScheduleById(Long scheduleId) {
        return doctorScheduleJpaRepository.findById(scheduleId);
    }

    public List<DoctorSchedule> findSchedules(Long doctorId, LocalDate date) {
        if (date == null) {
            return doctorScheduleJpaRepository.findByDoctorId(doctorId);
        }
        return doctorScheduleJpaRepository.findByDoctorIdAndDate(doctorId, date);
    }

    public List<DoctorSchedule> findAvailableSchedules(Long doctorId, LocalDate date) {
        if (date == null) {
            return doctorScheduleJpaRepository.findByDoctorIdAndStatus(
                    doctorId,
                    DoctorScheduleStatus.AVAILABLE
            );
        }
        return doctorScheduleJpaRepository.findByDoctorIdAndStatusAndDate(
                doctorId,
                DoctorScheduleStatus.AVAILABLE,
                date
        );
    }
}

interface DoctorJpaRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByUserId(Long userId);

    boolean existsByLicenseNumber(String licenseNumber);

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
}

interface DoctorScheduleJpaRepository extends JpaRepository<DoctorSchedule, Long> {

    boolean existsByDoctorIdAndDateAndStartTime(
            Long doctorId,
            LocalDate date,
            LocalTime startTime
    );

    List<DoctorSchedule> findByDoctorId(Long doctorId);

    List<DoctorSchedule> findByDoctorIdAndDate(Long doctorId, LocalDate date);

    List<DoctorSchedule> findByDoctorIdAndStatus(
            Long doctorId,
            DoctorScheduleStatus status
    );

    List<DoctorSchedule> findByDoctorIdAndStatusAndDate(
            Long doctorId,
            DoctorScheduleStatus status,
            LocalDate date
    );
}
