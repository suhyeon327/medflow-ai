package com.medflow.reservation.dto.response;

import com.medflow.reservation.entity.Reservation;
import com.medflow.reservation.entity.ReservationStatus;

public record ReservationStatusResponse(
        Long reservationId,
        ReservationStatus status
) {

    public static ReservationStatusResponse from(Reservation reservation) {
        return new ReservationStatusResponse(reservation.getId(), reservation.getStatus());
    }
}
