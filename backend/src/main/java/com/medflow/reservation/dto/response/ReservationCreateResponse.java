package com.medflow.reservation.dto.response;

import com.medflow.reservation.entity.Reservation;
import com.medflow.reservation.entity.ReservationStatus;

public record ReservationCreateResponse(
        Long reservationId,
        ReservationStatus status
) {

    public static ReservationCreateResponse from(
            Reservation reservation
    ) {
        return new ReservationCreateResponse(
                reservation.getId(),
                reservation.getStatus()
        );
    }
}
