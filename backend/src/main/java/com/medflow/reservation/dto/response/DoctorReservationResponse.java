package com.medflow.reservation.dto.response;

import com.medflow.reservation.entity.Reservation;
import com.medflow.reservation.entity.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record DoctorReservationResponse(
        Long reservationId,
        String patientName,
        LocalDate reservationDate,
        LocalTime startTime,
        LocalTime endTime,
        ReservationStatus reservationStatus
) {

    public static DoctorReservationResponse from(Reservation reservation) {
        return new DoctorReservationResponse(
                reservation.getId(),
                reservation.getPatient().getName(),
                reservation.getDoctorSchedule().getDate(),
                reservation.getDoctorSchedule().getStartTime(),
                reservation.getDoctorSchedule().getEndTime(),
                reservation.getStatus()
        );
    }
}
