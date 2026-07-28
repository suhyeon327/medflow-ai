package com.medflow.reservation.controller;

import com.medflow.auth.security.CustomUserDetails;
import com.medflow.common.response.ApiResponse;
import com.medflow.reservation.dto.response.ReservationDoctorApproveRejectResponse;
import com.medflow.reservation.dto.response.DoctorReservationResponse;
import com.medflow.reservation.service.DoctorReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/doctors/reservations")
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorReservationController {

    private final DoctorReservationService doctorReservationService;
    
    // 의사 담당 예약 목록 조회
    @GetMapping
    public ApiResponse<List<DoctorReservationResponse>> getDoctorReservations(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.success(
                doctorReservationService.getDoctorReservations(userDetails.getUserId())
        );
    }

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
