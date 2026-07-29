package com.medflow.reservation.dto.response;

import com.medflow.reservation.entity.Reservation;
import com.medflow.reservation.entity.ReservationStatus;

public record ReservationCompleteResponse(
        Long reservationId,
        ReservationStatus reservationStatus
) {

    public static ReservationCompleteResponse from(Reservation reservation) {
        return new ReservationCompleteResponse(
                reservation.getId(),
                reservation.getStatus()
        );
    }
}
