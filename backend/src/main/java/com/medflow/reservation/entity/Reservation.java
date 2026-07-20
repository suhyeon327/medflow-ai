package com.medflow.reservation.entity;

import com.medflow.common.entity.BaseEntity;
import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import com.medflow.doctor.entity.DoctorScheduleStatus;
import com.medflow.patient.entity.Patient;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Entity
@Table(name = "reservations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_schedule_id", nullable = false)
    private DoctorScheduleStatus doctorSchedule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    private Reservation(
            Patient patient,
            DoctorScheduleStatus doctorSchedule
    ){
        this.patient = patient;
        this.doctorSchedule = doctorSchedule;
        this.status = ReservationStatus.CONFIRMED;
    }

    // 예약 생성
    public static Reservation create(
            Patient patient,
            DoctorScheduleStatus doctorSchedule
    ){
        return new Reservation(
                patient,
                doctorSchedule
        );
    }

    // 예약 취소
    public void cancel(){

        if(status == ReservationStatus.COMPLETED){
            throw new BusinessException(ErrorCode.COMPLETED_RESERVATION);
        }

        this.status = ReservationStatus.CANCELLED;
    }

    // 진료 상태 변경
    public void changeStatus(ReservationStatus status) {

        validateChange(status);

        this.status = status;
    }


    private void validateChange(ReservationStatus nextStatus) {

        if (this.status == ReservationStatus.CANCELLED){
            throw new BusinessException(ErrorCode.INVALID_STATUS_CHANGE);
        }
    }
}
