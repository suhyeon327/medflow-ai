package com.medflow.reservation.repository;

import com.medflow.reservation.entity.Reservation;
import com.medflow.reservation.entity.ReservationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select r
            from Reservation r
            join fetch r.doctorSchedule ds
            where r.status = :status
              and (ds.date < :today or (ds.date = :today and ds.endTime <= :now))
            order by ds.date asc, ds.endTime asc, r.id asc
            """)
    List<Reservation> findCompletionTargets(
            @Param("status") ReservationStatus status,
            @Param("today") LocalDate today,
            @Param("now") LocalTime now,
            Pageable pageable
    );

}
