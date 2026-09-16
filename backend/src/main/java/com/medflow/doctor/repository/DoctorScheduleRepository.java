package com.medflow.doctor.repository;

import com.medflow.doctor.entity.DoctorSchedule;
import com.medflow.doctor.entity.DoctorScheduleStatus;
import com.medflow.doctor.entity.DoctorStatus;
import com.medflow.hospital.entity.HospitalStatus;
import com.medflow.user.entity.UserStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {

    // 현재 예약 가능한 진료 스케줄을 잠금 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select ds
            from DoctorSchedule ds
            join fetch ds.doctor d
            join fetch d.user u
            join fetch d.hospital h
            where ds.id = :scheduleId
              and ds.status = :scheduleStatus
              and d.status = :doctorStatus
              and u.status = :userStatus
              and h.status = :hospitalStatus
              and (ds.date > :currentDate
                or (ds.date = :currentDate and ds.startTime > :currentTime))
            """)
    Optional<DoctorSchedule> findReservableScheduleForUpdate(
            @Param("scheduleId") Long scheduleId,
            @Param("scheduleStatus") DoctorScheduleStatus scheduleStatus,
            @Param("doctorStatus") DoctorStatus doctorStatus,
            @Param("userStatus") UserStatus userStatus,
            @Param("hospitalStatus") HospitalStatus hospitalStatus,
            @Param("currentDate") LocalDate currentDate,
            @Param("currentTime") LocalTime currentTime
    );

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
