package com.medflow.reservation.entity;

import com.medflow.common.entity.BaseEntity;
import com.medflow.common.exception.BusinessException;
import com.medflow.common.exception.ErrorCode;
import com.medflow.doctor.entity.DoctorSchedule;
import com.medflow.patient.entity.Patient;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reservations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class  Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_schedule_id", nullable = false)
    private DoctorSchedule doctorSchedule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    private Reservation(
            Patient patient,
            DoctorSchedule doctorSchedule
    ){
        this.patient = patient;
        this.doctorSchedule = doctorSchedule;
        this.status = ReservationStatus.PENDING;
    }

    // 예약 생성
    public static Reservation create(
            Patient patient,
            DoctorSchedule doctorSchedule
    ){
        return new Reservation(
                patient,
                doctorSchedule
        );
    }

    // 예약 취소
    public void cancel() {

        if (status == ReservationStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.COMPLETED_RESERVATION);
        }

        if (status == ReservationStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.RESERVATION_ALREADY_CANCELLED);
        }

        this.status = ReservationStatus.CANCELLED;
    }

    // 예약 승인
    public void approve() {

        if (this.status != ReservationStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_CHANGE);
        }

        this.status = ReservationStatus.APPROVED;
    }

    // 예약 거절
    public void reject() {

        if (this.status != ReservationStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_CHANGE);
        }

        this.status = ReservationStatus.REJECTED;
    }

    // 진료 완료
    public void complete() {

        if (this.status != ReservationStatus.APPROVED) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_CHANGE);
        }

        this.status = ReservationStatus.COMPLETED;
    }
}
