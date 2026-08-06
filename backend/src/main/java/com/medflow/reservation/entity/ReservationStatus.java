package com.medflow.reservation.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReservationStatus {

    APPROVED("예약 승인"),
    COMPLETED("진료 완료"),
    CANCELLED("예약 취소");

    private final String description;
}
