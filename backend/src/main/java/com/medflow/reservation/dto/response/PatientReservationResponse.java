package com.medflow.reservation.dto.response;

import com.medflow.reservation.entity.Reservation;
import com.medflow.reservation.entity.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record PatientReservationResponse(
        Long reservationId,

        String hospitalName,

        String doctorName,

        LocalDate date,

        LocalTime startTime,

        LocalTime endTime,

        ReservationStatus status
) {
    public static PatientReservationResponse from(
            Reservation reservation
    ) {
        return new PatientReservationResponse(
                reservation.getId(),
                reservation.getDoctorSchedule()
                        .getDoctor()
                        .getHospital()
                        .getName(),
                reservation.getDoctorSchedule()
                        .getDoctor()
                        .getName(),
                reservation.getDoctorSchedule()
                        .getDate(),
                reservation.getDoctorSchedule()
                        .getStartTime(),
                reservation.getDoctorSchedule()
                        .getEndTime(),
                reservation.getStatus()
        );
    }
}
