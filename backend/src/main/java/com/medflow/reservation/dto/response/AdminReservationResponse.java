package com.medflow.reservation.dto.response;

import com.medflow.reservation.entity.Reservation;
import com.medflow.reservation.entity.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record AdminReservationResponse(
        Long reservationId,
        Long hospitalId,
        String hospitalName,
        Long doctorId,
        String doctorName,
        Long patientId,
        String patientName,
        LocalDate reservationDate,
        LocalTime startTime,
        LocalTime endTime,
        ReservationStatus reservationStatus,
        LocalDateTime createdAt
) {

    public static AdminReservationResponse from(Reservation reservation) {
        return new AdminReservationResponse(
                reservation.getId(),
                reservation.getDoctorSchedule().getDoctor().getHospital().getId(),
                reservation.getDoctorSchedule().getDoctor().getHospital().getName(),
                reservation.getDoctorSchedule().getDoctor().getId(),
                reservation.getDoctorSchedule().getDoctor().getName(),
                reservation.getPatient().getId(),
                reservation.getPatient().getName(),
                reservation.getDoctorSchedule().getDate(),
                reservation.getDoctorSchedule().getStartTime(),
                reservation.getDoctorSchedule().getEndTime(),
                reservation.getStatus(),
                reservation.getCreatedAt()
        );
    }
}
