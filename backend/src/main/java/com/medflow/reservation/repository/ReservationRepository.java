package com.medflow.reservation.repository;

import com.medflow.reservation.entity.Reservation;
import com.medflow.reservation.entity.ReservationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.time.LocalTime;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 병원에 미처리된 미래 예약이 존재하는지 확인
    @Query("""
            select (count(r) > 0)
            from Reservation r
            join r.doctorSchedule ds
            join ds.doctor d
            where d.hospital.id = :hospitalId
              and r.status = :status
              and (ds.date > :currentDate
                or (ds.date = :currentDate and ds.startTime > :currentTime))
            """)
    boolean existsUpcomingReservationByHospitalIdAndStatus(
            @Param("hospitalId") Long hospitalId,
            @Param("status") ReservationStatus status,
            @Param("currentDate") LocalDate currentDate,
            @Param("currentTime") LocalTime currentTime
    );

    List<Reservation> findByPatientId(Long patientId);

    Optional<Reservation> findByIdAndPatientId(Long id, Long patientId);

    Optional<Reservation> findByIdAndDoctorScheduleDoctorId(Long id, Long doctorId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select r
            from Reservation r
            join fetch r.doctorSchedule ds
            where r.id = :reservationId
              and ds.doctor.id = :doctorId
            """)
    Optional<Reservation> findDoctorReservationForUpdate(
            @Param("reservationId") Long reservationId,
            @Param("doctorId") Long doctorId
    );

}
