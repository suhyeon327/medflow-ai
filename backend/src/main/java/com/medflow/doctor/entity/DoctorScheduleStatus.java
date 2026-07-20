package com.medflow.doctor.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DoctorScheduleStatus {

    AVAILABLE("예약 가능"),
    RESERVED("예약 완료"),
    BLOCKED("예약 불가");

    private final String description;
}
