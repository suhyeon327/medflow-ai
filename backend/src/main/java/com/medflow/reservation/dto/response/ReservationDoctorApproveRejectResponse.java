package com.medflow.reservation.dto.response;

import com.medflow.reservation.entity.Reservation;
import com.medflow.reservation.entity.ReservationStatus;

public record ReservationDoctorApproveRejectResponse(
        Long reservationId,
        ReservationStatus status
) {

    public static ReservationDoctorApproveRejectResponse from(Reservation reservation) {
        return new ReservationDoctorApproveRejectResponse(
                reservation.getId(),
                reservation.getStatus()
        );
    }
}
