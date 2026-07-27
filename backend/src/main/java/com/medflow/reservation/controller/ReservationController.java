package com.medflow.reservation.controller;

import com.medflow.auth.security.CustomUserDetails;
import com.medflow.common.response.ApiResponse;
import com.medflow.reservation.dto.request.ReservationCreateRequest;
import com.medflow.reservation.dto.response.ReservationCreateResponse;
import com.medflow.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
