package com.medflow.doctor.entity;

import com.medflow.common.entity.BaseEntity;
import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Entity
@Table(name = "doctor_schedule")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DoctorSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;   // 담당 의사

    private LocalDate date;   // 진료 날짜

    private LocalTime startTime;   // 진료 시작 시간

    private LocalTime endTime;   // 진료 종료 시간

    @Enumerated(EnumType.STRING)
    private DoctorScheduleStatus status;   // 예약 상태

    // 진료 스케줄 등록
    public static DoctorSchedule create(
            Doctor doctor,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime
    ) {
        DoctorSchedule doctorSchedule = new DoctorSchedule();

        doctorSchedule.doctor = doctor;
        doctorSchedule.date = date;
        doctorSchedule.startTime = startTime;
        doctorSchedule.endTime = endTime;
        doctorSchedule.status = DoctorScheduleStatus.AVAILABLE;

        return doctorSchedule;
    }

    // 예약 처리
    public void reserve() {

        if (status != DoctorScheduleStatus.AVAILABLE) {
            throw new BusinessException(ErrorCode.SCHEDULE_NOT_AVAILABLE);
        }

        this.status = DoctorScheduleStatus.RESERVED;
    }

    // 예약 취소 후 복구
    public void release() {

        this.status = DoctorScheduleStatus.AVAILABLE;
    }

}
