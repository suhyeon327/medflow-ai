package com.medflow.reservation.repository;

import com.medflow.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByPatientId(Long patientId);

    Optional<Reservation> findByIdAndPatientId(Long id, Long patientId);

    Optional<Reservation> findByIdAndDoctorScheduleDoctorId(Long id, Long doctorId);

}
