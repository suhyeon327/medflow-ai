package com.medflow.reservation.repository;

import com.medflow.doctor.entity.QDoctor;
import com.medflow.doctor.entity.QDoctorSchedule;
import com.medflow.hospital.entity.QHospital;
import com.medflow.patient.entity.QPatient;
import com.medflow.reservation.entity.QReservation;
import com.medflow.reservation.entity.Reservation;
import com.medflow.reservation.entity.ReservationPeriod;
import com.medflow.reservation.entity.ReservationStatus;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PatientReservationSearchRepository {

    private final JPAQueryFactory queryFactory;

    public Page<Reservation> search(
            Long patientId,
            ReservationStatus status,
            LocalDate date,
            Long hospitalId,
            Long doctorId,
            ReservationPeriod period,
            Pageable pageable
    ) {
        QReservation reservation = QReservation.reservation;
        QDoctorSchedule doctorSchedule = QDoctorSchedule.doctorSchedule;
        QDoctor doctor = QDoctor.doctor;
        QHospital hospital = QHospital.hospital;
        QPatient patient = QPatient.patient;

        List<Reservation> content = queryFactory
                .selectFrom(reservation)
                .join(reservation.doctorSchedule, doctorSchedule).fetchJoin()
                .join(doctorSchedule.doctor, doctor).fetchJoin()
                .join(doctor.hospital, hospital).fetchJoin()
                .join(reservation.patient, patient).fetchJoin()
                .where(
                        patientIdEq(patient, patientId),
                        statusEq(reservation, status),
                        dateEq(doctorSchedule, date),
                        hospitalIdEq(hospital, hospitalId),
                        doctorIdEq(doctor, doctorId),
                        periodEq(doctorSchedule, period)
                )
                .orderBy(orderSpecifiers(doctorSchedule, period))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(reservation.count())
                .from(reservation)
                .join(reservation.doctorSchedule, doctorSchedule)
                .join(doctorSchedule.doctor, doctor)
                .join(doctor.hospital, hospital)
                .join(reservation.patient, patient)
                .where(
                        patientIdEq(patient, patientId),
                        statusEq(reservation, status),
                        dateEq(doctorSchedule, date),
                        hospitalIdEq(hospital, hospitalId),
                        doctorIdEq(doctor, doctorId),
                        periodEq(doctorSchedule, period)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanExpression patientIdEq(QPatient patient, Long patientId) {
        return patient.id.eq(patientId);
    }

    private BooleanExpression statusEq(QReservation reservation, ReservationStatus status) {
        return status == null ? null : reservation.status.eq(status);
    }

    private BooleanExpression dateEq(QDoctorSchedule doctorSchedule, LocalDate date) {
        return date == null ? null : doctorSchedule.date.eq(date);
    }

    private BooleanExpression hospitalIdEq(QHospital hospital, Long hospitalId) {
        return hospitalId == null ? null : hospital.id.eq(hospitalId);
    }

    private BooleanExpression doctorIdEq(QDoctor doctor, Long doctorId) {
        return doctorId == null ? null : doctor.id.eq(doctorId);
    }

    private BooleanExpression periodEq(QDoctorSchedule doctorSchedule, ReservationPeriod period) {
        if (period == ReservationPeriod.UPCOMING) {
            return doctorSchedule.date.gt(LocalDate.now());
        }

        if (period == ReservationPeriod.TODAY) {
            return doctorSchedule.date.eq(LocalDate.now());
        }

        if (period == ReservationPeriod.PAST) {
            return doctorSchedule.date.lt(LocalDate.now());
        }

        return null;
    }

    private OrderSpecifier<?>[] orderSpecifiers(
            QDoctorSchedule doctorSchedule,
            ReservationPeriod period
    ) {
        if (period == ReservationPeriod.PAST) {
            return new OrderSpecifier[]{
                    doctorSchedule.date.desc(),
                    doctorSchedule.startTime.desc()
            };
        }

        if (period == ReservationPeriod.UPCOMING) {
            return new OrderSpecifier[]{
                    doctorSchedule.date.asc(),
                    doctorSchedule.startTime.asc()
            };
        }

        return new OrderSpecifier[]{
                doctorSchedule.date.desc(),
                doctorSchedule.startTime.asc()
        };
    }
}
