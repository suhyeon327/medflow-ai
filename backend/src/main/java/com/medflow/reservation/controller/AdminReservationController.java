package com.medflow.reservation.controller;

import com.medflow.common.response.ApiResponse;
import com.medflow.reservation.dto.response.AdminReservationPageResponse;
import com.medflow.reservation.entity.ReservationStatus;
import com.medflow.reservation.service.AdminReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/reservations")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReservationController {

    private final AdminReservationService adminReservationService;

    // 관리자 전체 예약 조회 및 검색
    @GetMapping
    public ApiResponse<AdminReservationPageResponse> searchReservations(
            @RequestParam(required = false) Long hospitalId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) ReservationStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.success(
                adminReservationService.searchReservations(
                        hospitalId,
                        doctorId,
                        patientId,
                        date,
                        status,
                        pageable
                )
        );
    }
}
