package com.medflow.doctor.repository;

import com.medflow.doctor.entity.DoctorSchedule;
import com.medflow.doctor.entity.DoctorScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {

    // 동일 의사와 날짜에 겹치는 진료 스케줄 존재 여부
    @Query("""
            select (count(ds) > 0)
            from DoctorSchedule ds
            where ds.doctor.id = :doctorId
              and ds.date = :date
              and ds.startTime < :endTime
              and ds.endTime > :startTime
            """)
    boolean existsOverlappingSchedule(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
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
