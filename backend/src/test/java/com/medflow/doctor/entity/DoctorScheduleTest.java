package com.medflow.doctor.entity;

import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class DoctorScheduleTest {

    @Test
    void create_initializesScheduleAsAvailable() {
        Doctor doctor = mock(Doctor.class);

        DoctorSchedule schedule = DoctorSchedule.create(
                doctor, LocalDate.of(2026, 8, 10), LocalTime.of(9, 0), LocalTime.of(9, 30));

        assertThat(schedule.getDoctor()).isSameAs(doctor);
        assertThat(schedule.getStatus()).isEqualTo(DoctorScheduleStatus.AVAILABLE);
    }

    @Test
    void reserve_changesAvailableScheduleToReserved() {
        DoctorSchedule schedule = schedule();

        schedule.reserve();

        assertThat(schedule.getStatus()).isEqualTo(DoctorScheduleStatus.RESERVED);
    }

    @Test
    void reserve_rejectsAlreadyReservedSchedule() {
        DoctorSchedule schedule = schedule();
        schedule.reserve();

        assertThatThrownBy(schedule::reserve)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SCHEDULE_NOT_AVAILABLE);
    }

    @Test
    void release_changesReservedScheduleToAvailable() {
        DoctorSchedule schedule = schedule();
        schedule.reserve();

        schedule.release();

        assertThat(schedule.getStatus()).isEqualTo(DoctorScheduleStatus.AVAILABLE);
    }

    private DoctorSchedule schedule() {
        return DoctorSchedule.create(
                mock(Doctor.class),
                LocalDate.of(2026, 8, 10),
                LocalTime.of(9, 0),
                LocalTime.of(9, 30)
        );
    }
}
