package com.medflow.reservation.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReservationStatus {

    REQUESTED("예약 요청"),
    CONFIRMED("예약 확정"),
    COMPLETED("진료 완료"),
    CANCELLED("예약 취소");

    private final String description;
}
