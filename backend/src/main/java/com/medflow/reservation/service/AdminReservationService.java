package com.medflow.reservation.service;

import com.medflow.reservation.dto.response.AdminReservationPageResponse;
import com.medflow.reservation.dto.response.AdminReservationResponse;
import com.medflow.reservation.entity.ReservationStatus;
import com.medflow.reservation.repository.AdminReservationSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReservationService {

    private final AdminReservationSearchRepository adminReservationSearchRepository;
    
    // 관리자 전체 예약 조회 및 검색
    public AdminReservationPageResponse searchReservations(
            Long hospitalId,
            Long doctorId,
            Long patientId,
            LocalDate date,
            ReservationStatus status,
            Pageable pageable
    ) {
        Pageable validatedPageable = validatedPageable(pageable);

        Page<AdminReservationResponse> reservationPage = adminReservationSearchRepository
                .search(hospitalId, doctorId, patientId, date, status, validatedPageable)
                .map(AdminReservationResponse::from);

        return AdminReservationPageResponse.from(reservationPage);
    }

    private Pageable validatedPageable(Pageable pageable) {

        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Order.desc("reservationDate"), Sort.Order.asc("startTime"))
            );
        }

        return pageable;
    }
}
