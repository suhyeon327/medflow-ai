package com.medflow.doctor.repository;

import com.medflow.doctor.entity.DoctorSchedule;
import com.medflow.doctor.entity.DoctorScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {

    // 의사 스케줄 존재 여부
    boolean existsByDoctorIdAndDateAndStartTime(
            Long doctorId,
            LocalDate date,
            LocalTime startTime
    );

    List<DoctorSchedule> findByDoctorId(Long doctorId);

    List<DoctorSchedule> findByDoctorIdAndDate(Long doctorId, LocalDate date);

    List<DoctorSchedule> findByDoctorIdAndStatus(Long doctorId, DoctorScheduleStatus status);

    List<DoctorSchedule> findByDoctorIdAndStatusAndDate(
            Long doctorId,
            DoctorScheduleStatus status,
            LocalDate date
    );
}
