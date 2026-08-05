package com.medflow.doctor.controller;

import com.medflow.auth.security.CustomUserDetails;
import com.medflow.common.response.ApiResponse;
import com.medflow.doctor.dto.request.DoctorScheduleCreateRequest;
import com.medflow.doctor.dto.request.DoctorUpdateRequest;
import com.medflow.doctor.dto.response.DoctorProfileResponse;
import com.medflow.doctor.dto.response.DoctorScheduleResponse;
import com.medflow.doctor.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
@RequestMapping("/api/v1/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping("/profile")
    public ApiResponse<DoctorProfileResponse> getDoctorProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.success(doctorService.getDoctorProfile(userDetails.getUserId()));
    }

    @PutMapping("/profile")
    public ApiResponse<DoctorProfileResponse> updateDoctorProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody DoctorUpdateRequest request
    ) {
        return ApiResponse.success(
                doctorService.updateDoctorProfile(userDetails.getUserId(), request)
        );
    }

    @PostMapping("/schedules")
    public ApiResponse<List<DoctorScheduleResponse>> createDoctorSchedules(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody DoctorScheduleCreateRequest request
    ) {
        return ApiResponse.success(
                doctorService.createDoctorSchedules(userDetails.getUserId(), request)
        );
    }

    @GetMapping("/schedules")
    public ApiResponse<List<DoctorScheduleResponse>> getDoctorSchedules(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.success(
                doctorService.getDoctorSchedules(userDetails.getUserId(), date)
        );
    }
}
