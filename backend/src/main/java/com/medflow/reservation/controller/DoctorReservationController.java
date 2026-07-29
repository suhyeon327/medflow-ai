package com.medflow.reservation.controller;

import com.medflow.auth.security.CustomUserDetails;
import com.medflow.common.response.ApiResponse;
import com.medflow.reservation.dto.response.ReservationDoctorApproveRejectResponse;
import com.medflow.reservation.dto.response.DoctorReservationResponse;
import com.medflow.reservation.dto.response.DoctorReservationPatientResponse;
import com.medflow.reservation.dto.response.ReservationCompleteResponse;
import com.medflow.reservation.dto.response.DoctorReservationPageResponse;
import com.medflow.reservation.entity.ReservationStatus;
import com.medflow.reservation.service.DoctorReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.util.List;
import java.time.LocalDate;

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
    
    // 오늘 예약 조회
    @GetMapping("/today")
    public ApiResponse<List<DoctorReservationResponse>> getTodayDoctorReservations(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.success(
                doctorReservationService.getTodayDoctorReservations(userDetails.getUserId())
        );
    }
    
    // 날짜별 예약 조회
    @GetMapping("/date")
    public ApiResponse<List<DoctorReservationResponse>> getDoctorReservationsByDate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.success(
                doctorReservationService.getDoctorReservationsByDate(userDetails.getUserId(), date)
        );
    }

    // 의사 예약 검색 및 필터링
    @GetMapping("/search")
    public ApiResponse<DoctorReservationPageResponse> searchDoctorReservations(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) ReservationStatus status,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ApiResponse.success(
                doctorReservationService.searchDoctorReservations(
                        userDetails.getUserId(), date, status, pageable
                )
        );
    }

    // 환자 정보 조회
    @GetMapping("/{reservationId}/patient")
    public ApiResponse<DoctorReservationPatientResponse> getReservationPatient(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reservationId
    ) {
        return ApiResponse.success(
                doctorReservationService.getReservationPatient(userDetails.getUserId(), reservationId)
        );
    }

    // 진료 완료
    @PatchMapping("/{reservationId}/complete")
    public ApiResponse<ReservationCompleteResponse> completeReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reservationId
    ) {
        return ApiResponse.success(
                doctorReservationService.completeReservation(userDetails.getUserId(), reservationId)
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
