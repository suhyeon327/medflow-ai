package com.medflow.reservation.controller;

import com.medflow.auth.security.CustomUserDetails;
import com.medflow.common.response.ApiResponse;
import com.medflow.reservation.dto.request.ReservationCreateRequest;
import com.medflow.reservation.dto.response.PatientReservationResponse;
import com.medflow.reservation.dto.response.ReservationCancelResponse;
import com.medflow.reservation.dto.response.ReservationCreateResponse;
import com.medflow.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    // 예약 생성
    @PostMapping("/")
    @PreAuthorize("hasRole('PATIENT')")
    public ApiResponse<ReservationCreateResponse> createReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReservationCreateRequest request
    ) {
        return ApiResponse.success(
                reservationService.createReservation(
                        userDetails.getUserId(),
                        request
                )
        );
    }

    // 환자 예약 내역 조회
    @GetMapping("/patient")
    @PreAuthorize("hasRole('PATIENT')")
    public ApiResponse<List<PatientReservationResponse>> getPatientReservations(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.success(
                reservationService.getPatientReservations(
                userDetails.getUserId())
        );
    }

    // 환자 예약 취소
    @PatchMapping("/{reservationId}/cancel")
    @PreAuthorize("hasRole('PATIENT')")
    public ApiResponse<ReservationCancelResponse> cancelReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reservationId
    ) {
        return ApiResponse.success(
                reservationService.cancelReservation(
                        userDetails.getUserId(),
                        reservationId
                )
        );
    }
}
