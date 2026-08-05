package com.medflow.reservation.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReservationStatus {

    PENDING("예약 대기"),
    APPROVED("예약 승인"),
    REJECTED("예약 거절"),
    COMPLETED("진료 완료"),
    CANCELLED("예약 취소");

    private final String description;
}
