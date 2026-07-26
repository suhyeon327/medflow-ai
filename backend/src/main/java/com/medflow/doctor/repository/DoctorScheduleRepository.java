package com.medflow.doctor.repository;

import com.medflow.doctor.entity.Doctor;
import com.medflow.doctor.entity.DoctorSchedule;
import com.medflow.doctor.entity.DoctorScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {

    // 의사 스케줄 존재 여부
    boolean existsByDoctorIdAndDateAndStartTime(
            Long doctorId,
            LocalDate date,
            LocalTime startTime
    );

    List<DoctorSchedule> findByDoctorIdAndStatus(
            Long doctorId, DoctorScheduleStatus status
    );
}
