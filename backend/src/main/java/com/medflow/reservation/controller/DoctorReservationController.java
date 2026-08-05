package com.medflow.reservation.controller;

import com.medflow.auth.security.CustomUserDetails;
import com.medflow.common.response.ApiResponse;
import com.medflow.reservation.dto.request.ReservationStatusUpdateRequest;
import com.medflow.reservation.dto.response.DoctorReservationPageResponse;
import com.medflow.reservation.dto.response.DoctorReservationPatientResponse;
import com.medflow.reservation.dto.response.ReservationStatusResponse;
import com.medflow.reservation.entity.ReservationStatus;
import com.medflow.reservation.service.DoctorReservationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/doctors/reservations")
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorReservationController {

    private final DoctorReservationService doctorReservationService;

    @Operation(summary = "의사 예약 목록 조회", description = "예약 날짜와 상태를 선택적으로 조합하여 조회합니다.")
    @GetMapping
    public ApiResponse<DoctorReservationPageResponse> getDoctorReservations(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) ReservationStatus status,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ApiResponse.success(
                doctorReservationService.getDoctorReservations(userDetails.getUserId(), date, status, pageable)
        );
    }

    @Operation(summary = "예약 환자 정보 조회")
    @GetMapping("/{reservationId}/patient")
    public ApiResponse<DoctorReservationPatientResponse> getReservationPatient(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reservationId
    ) {
        return ApiResponse.success(
                doctorReservationService.getReservationPatient(userDetails.getUserId(), reservationId)
        );
    }

    @Operation(summary = "예약 상태 변경", description = "APPROVED, REJECTED 상태로 변경하거나 종료 시간이 지난 APPROVED 예약을 COMPLETED로 변경할 수 있습니다.")
    @PatchMapping("/{reservationId}/status")
    public ApiResponse<ReservationStatusResponse> updateReservationStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reservationId,
            @Valid @RequestBody ReservationStatusUpdateRequest request
    ) {
        return ApiResponse.success(
                doctorReservationService.updateReservationStatus(
                        userDetails.getUserId(), reservationId, request.status()
                )
        );
    }
}
