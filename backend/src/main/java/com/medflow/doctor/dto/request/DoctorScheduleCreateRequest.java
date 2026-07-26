package com.medflow.doctor.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record DoctorScheduleCreateRequest(

        @NotNull
        LocalDate date,

        @NotNull
        LocalTime startTime,

        @NotNull
        LocalTime endTime,

        @NotNull
        @Min(10)
        @Max(60)
        Integer slotMinutes
) {
}
