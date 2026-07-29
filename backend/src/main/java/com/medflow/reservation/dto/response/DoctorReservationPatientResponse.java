package com.medflow.reservation.dto.response;

import com.medflow.patient.entity.Patient;
import com.medflow.patient.entity.Gender;
import com.medflow.reservation.entity.Reservation;
import com.medflow.reservation.entity.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record DoctorReservationPatientResponse(
        Long patientId,
        String patientName,
        Gender gender,
        LocalDate birthDate,
        String phoneNumber,
        Long reservationId,
        LocalDate reservationDate,
        LocalTime startTime,
        LocalTime endTime,
        ReservationStatus reservationStatus
) {

    public static DoctorReservationPatientResponse from(Reservation reservation) {

        Patient patient = reservation.getPatient();

        return new DoctorReservationPatientResponse(
                patient.getId(),
                patient.getName(),
                patient.getGender(),
                patient.getBirth(),
                patient.getPhone(),
                reservation.getId(),
                reservation.getDoctorSchedule().getDate(),
                reservation.getDoctorSchedule().getStartTime(),
                reservation.getDoctorSchedule().getEndTime(),
                reservation.getStatus()
        );
    }
}
