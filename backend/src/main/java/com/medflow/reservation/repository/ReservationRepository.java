package com.medflow.reservation.repository;

import com.medflow.reservation.entity.Reservation;
import com.medflow.reservation.entity.ReservationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByPatientId(Long patientId);

    Optional<Reservation> findByIdAndPatientId(Long id, Long patientId);

    Optional<Reservation> findByIdAndDoctorScheduleDoctorId(Long id, Long doctorId);

    @EntityGraph(attributePaths = {"patient", "doctorSchedule"})
    List<Reservation> findAllByDoctorScheduleDoctorIdOrderByDoctorScheduleDateAscDoctorScheduleStartTimeAsc(Long doctorId);

    @EntityGraph(attributePaths = {"patient", "doctorSchedule"})
    @Query("""
            select r
            from Reservation r
            join r.doctorSchedule ds
            where ds.doctor.id = :doctorId
              and ds.date = :date
              and r.status in :statuses
            order by ds.startTime asc
            """)
    List<Reservation> findTodayReservationsByDoctorId(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date,
            @Param("statuses") List<ReservationStatus> statuses
    );

    @EntityGraph(attributePaths = {"patient", "doctorSchedule"})
    List<Reservation> findAllByDoctorScheduleDoctorIdAndDoctorScheduleDateOrderByDoctorScheduleStartTimeAsc(
            Long doctorId,
            LocalDate date
    );
}
