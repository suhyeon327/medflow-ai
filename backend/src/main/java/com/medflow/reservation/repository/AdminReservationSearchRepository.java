package com.medflow.reservation.repository;

import com.medflow.doctor.entity.QDoctor;
import com.medflow.doctor.entity.QDoctorSchedule;
import com.medflow.hospital.entity.QHospital;
import com.medflow.patient.entity.QPatient;
import com.medflow.reservation.entity.QReservation;
import com.medflow.reservation.entity.Reservation;
import com.medflow.reservation.entity.ReservationStatus;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AdminReservationSearchRepository {

    private final JPAQueryFactory queryFactory;

    public Page<Reservation> search(
            Long hospitalId,
            Long doctorId,
            Long patientId,
            LocalDate date,
            ReservationStatus status,
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
                        hospitalIdEq(hospital, hospitalId),
                        doctorIdEq(doctor, doctorId),
                        patientIdEq(patient, patientId),
                        dateEq(doctorSchedule, date),
                        statusEq(reservation, status)
                )
                .orderBy(orderSpecifiers(reservation, doctorSchedule, pageable))
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
                        hospitalIdEq(hospital, hospitalId),
                        doctorIdEq(doctor, doctorId),
                        patientIdEq(patient, patientId),
                        dateEq(doctorSchedule, date),
                        statusEq(reservation, status)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanExpression hospitalIdEq(QHospital hospital, Long hospitalId) {
        return hospitalId == null ? null : hospital.id.eq(hospitalId);
    }

    private BooleanExpression doctorIdEq(QDoctor doctor, Long doctorId) {
        return doctorId == null ? null : doctor.id.eq(doctorId);
    }

    private BooleanExpression patientIdEq(QPatient patient, Long patientId) {
        return patientId == null ? null : patient.id.eq(patientId);
    }

    private BooleanExpression dateEq(QDoctorSchedule doctorSchedule, LocalDate date) {
        return date == null ? null : doctorSchedule.date.eq(date);
    }

    private BooleanExpression statusEq(QReservation reservation, ReservationStatus status) {
        return status == null ? null : reservation.status.eq(status);
    }

    private OrderSpecifier<?>[] orderSpecifiers(
            QReservation reservation,
            QDoctorSchedule doctorSchedule,
            Pageable pageable
    ) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        for (Sort.Order sortOrder : pageable.getSort()) {
            Order direction = sortOrder.isAscending() ? Order.ASC : Order.DESC;

            switch (sortOrder.getProperty()) {
                case "reservationDate" -> orderSpecifiers.add(new OrderSpecifier<>(direction, doctorSchedule.date));
                case "startTime" -> orderSpecifiers.add(new OrderSpecifier<>(direction, doctorSchedule.startTime));
                case "reservationStatus" -> orderSpecifiers.add(new OrderSpecifier<>(direction, reservation.status));
                case "createdAt" -> orderSpecifiers.add(new OrderSpecifier<>(direction, reservation.createdAt));
                default -> {
                }
            }
        }

        if (orderSpecifiers.isEmpty()) {
            orderSpecifiers.add(doctorSchedule.date.desc());
            orderSpecifiers.add(doctorSchedule.startTime.asc());
        }

        return orderSpecifiers.toArray(OrderSpecifier[]::new);
    }
}
