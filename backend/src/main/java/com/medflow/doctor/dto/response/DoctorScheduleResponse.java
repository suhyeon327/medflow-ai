package com.medflow.doctor.dto.response;

import com.medflow.doctor.entity.DoctorSchedule;

import java.time.LocalDate;
import java.time.LocalTime;

public record DoctorScheduleResponse(
        Long scheduleId,

        LocalDate date,

        LocalTime startTime,

        LocalTime endTime
) {

    public static DoctorScheduleResponse from(
            DoctorSchedule doctorSchedule
    ){

        return new DoctorScheduleResponse(

                doctorSchedule.getId(),

                doctorSchedule.getDate(),

                doctorSchedule.getStartTime(),

                doctorSchedule.getEndTime()

        );

    }
}
