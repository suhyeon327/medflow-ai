package com.medflow.reservation.repository;

import com.medflow.patient.entity.QPatient;
import com.medflow.doctor.entity.QDoctorSchedule;
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
public class DoctorReservationSearchRepository {

    private final JPAQueryFactory queryFactory;

    public Page<Reservation> search(
            Long doctorId,   // 로그인한 의사의 ID
            LocalDate date,   // 검색할 예약 날짜, 없으면 전체 날짜
            ReservationStatus status,   // 검색할 예약 상태, 없으면 전체 상태
            Pageable pageable   // 페이지 번호, 페이지 크기, 정렬 조건
    ) {

        // Q 클래스 선언
        QReservation reservation = QReservation.reservation;
        QDoctorSchedule doctorSchedule = QDoctorSchedule.doctorSchedule;
        QPatient patient = QPatient.patient;

        // 실제 예약 목록 조회
        List<Reservation> content = queryFactory
                .selectFrom(reservation)
                .join(reservation.doctorSchedule, doctorSchedule).fetchJoin()
                .join(reservation.patient, patient).fetchJoin()
                .where(
                        doctorIdEq(doctorSchedule, doctorId),
                        dateEq(doctorSchedule, date),
                        statusEq(reservation, status)
                )
                .orderBy(orderSpecifiers(doctorSchedule, reservation, pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 개수 조회
        Long total = queryFactory
                .select(reservation.count())
                .from(reservation)
                .join(reservation.doctorSchedule, doctorSchedule)
                .where(
                        doctorIdEq(doctorSchedule, doctorId),
                        dateEq(doctorSchedule, date),
                        statusEq(reservation, status)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanExpression doctorIdEq(QDoctorSchedule doctorSchedule, Long doctorId) {
        return doctorSchedule.doctor.id.eq(doctorId);
    }

    private BooleanExpression dateEq(QDoctorSchedule doctorSchedule, LocalDate date) {
        return date == null ? null : doctorSchedule.date.eq(date);
    }

    private BooleanExpression statusEq(QReservation reservation, ReservationStatus status) {
        return status == null ? null : reservation.status.eq(status);
    }

    private OrderSpecifier<?>[] orderSpecifiers(
            QDoctorSchedule doctorSchedule,
            QReservation reservation,
            Pageable pageable
    ) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        for (Sort.Order sortOrder : pageable.getSort()) {
            Order direction = sortOrder.isAscending() ? Order.ASC : Order.DESC;

            switch (sortOrder.getProperty()) {
                case "date" -> orderSpecifiers.add(new OrderSpecifier<>(direction, doctorSchedule.date));
                case "startTime" -> orderSpecifiers.add(new OrderSpecifier<>(direction, doctorSchedule.startTime));
                case "status" -> orderSpecifiers.add(new OrderSpecifier<>(direction, reservation.status));
                default -> {
                }
            }
        }

        if (orderSpecifiers.isEmpty()) {
            orderSpecifiers.add(doctorSchedule.date.asc());
            orderSpecifiers.add(doctorSchedule.startTime.asc());
        }

        return orderSpecifiers.toArray(OrderSpecifier[]::new);
    }
}
