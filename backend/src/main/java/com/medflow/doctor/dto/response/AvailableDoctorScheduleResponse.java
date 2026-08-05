package com.medflow.doctor.dto.response;

import com.medflow.doctor.entity.DoctorSchedule;

import java.time.LocalDate;
import java.time.LocalTime;

public record AvailableDoctorScheduleResponse(
        Long scheduleId,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime
) {

    public static AvailableDoctorScheduleResponse from(DoctorSchedule schedule) {
        return new AvailableDoctorScheduleResponse(
                schedule.getId(),
                schedule.getDate(),
                schedule.getStartTime(),
                schedule.getEndTime()
        );
    }
}
