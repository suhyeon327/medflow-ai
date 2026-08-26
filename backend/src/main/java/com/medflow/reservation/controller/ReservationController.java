package com.medflow.reservation.controller;

import com.medflow.security.principal.UserPrincipal;
import com.medflow.common.response.ApiResponse;
import com.medflow.reservation.dto.request.ReservationCreateRequest;
import com.medflow.reservation.dto.response.PatientReservationResponse;
import com.medflow.reservation.dto.response.PatientReservationPageResponse;
import com.medflow.reservation.entity.ReservationStatus;
import com.medflow.reservation.entity.ReservationPeriod;
import com.medflow.reservation.dto.response.ReservationCancelResponse;
import com.medflow.reservation.dto.response.ReservationCreateResponse;
import com.medflow.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import java.util.List;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
@RequestMapping("/api/v1/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    // 예약 생성
    @PostMapping("/")
    public ApiResponse<ReservationCreateResponse> createReservation(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ReservationCreateRequest request
    ) {
        return ApiResponse.success(
                reservationService.createReservation(
                        userPrincipal.getUserId(),
                        request
                )
        );
    }

    // 환자 예약 내역 조회
    @GetMapping("/patient")
    public ApiResponse<PatientReservationPageResponse> getPatientReservations(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long hospitalId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) ReservationPeriod period,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ApiResponse.success(
                reservationService.getPatientReservations(userPrincipal.getUserId(), status, date, hospitalId, doctorId, period, pageable)
        );
    }

    // 환자 예약 취소
    @PatchMapping("/{reservationId}/cancel")
    public ApiResponse<ReservationCancelResponse> cancelReservation(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long reservationId
    ) {
        return ApiResponse.success(
                reservationService.cancelReservation(
                        userPrincipal.getUserId(),
                        reservationId
                )
        );
    }
}
