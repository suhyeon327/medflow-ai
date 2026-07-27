package com.medflow.reservation.dto.response;

import com.medflow.reservation.entity.Reservation;
import com.medflow.reservation.entity.ReservationStatus;

public record ReservationCancelResponse(
        Long reservationId,
        ReservationStatus status
) {

    public static ReservationCancelResponse from(Reservation reservation) {
        return new ReservationCancelResponse(
                reservation.getId(),
                reservation.getStatus()
        );
    }
}
