package com.medflow.reservation.dto.response;

import com.medflow.reservation.entity.Reservation;
import com.medflow.reservation.entity.ReservationStatus;

public record ReservationCompletedResponse(
        Long reservationId,
        ReservationStatus reservationStatus
) {

    public static ReservationCompletedResponse from(Reservation reservation) {
        return new ReservationCompletedResponse(
                reservation.getId(),
                reservation.getStatus()
        );
    }
}
