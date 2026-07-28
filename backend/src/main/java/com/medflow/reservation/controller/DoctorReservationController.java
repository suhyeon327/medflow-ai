package com.medflow.reservation.controller;

import com.medflow.auth.security.CustomUserDetails;
import com.medflow.common.response.ApiResponse;
import com.medflow.reservation.dto.response.ReservationDoctorApproveRejectResponse;
import com.medflow.reservation.service.DoctorReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/doctors/reservations")
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorReservationController {

    private final DoctorReservationService doctorReservationService;

    // 예약 승인
    @PatchMapping("/{reservationId}/approve")
    public ApiResponse<ReservationDoctorApproveRejectResponse> approveReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reservationId
    ) {
        return ApiResponse.success(
                doctorReservationService.approveReservation(userDetails.getUserId(), reservationId)
        );
    }

    // 예약 거절
    @PatchMapping("/{reservationId}/reject")
    public ApiResponse<ReservationDoctorApproveRejectResponse> rejectReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reservationId
    ) {
        return ApiResponse.success(
                doctorReservationService.rejectReservation(userDetails.getUserId(), reservationId)
        );
    }
}
