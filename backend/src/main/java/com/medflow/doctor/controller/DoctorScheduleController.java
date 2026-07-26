package com.medflow.doctor.controller;

import com.medflow.auth.security.CustomUserDetails;
import com.medflow.common.response.ApiResponse;
import com.medflow.doctor.dto.request.DoctorScheduleCreateRequest;
import com.medflow.doctor.dto.response.DoctorScheduleResponse;
import com.medflow.doctor.service.DoctorScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/doctors")
public class DoctorScheduleController {

    private final DoctorScheduleService doctorScheduleService;

    // 의사 진료 스케줄 등록
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    @PostMapping("/schedules")
    public ApiResponse<List<DoctorScheduleResponse>> createDoctorSchedules(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody DoctorScheduleCreateRequest request
    ) {
        return ApiResponse.success(
                doctorScheduleService.createSchedule(userDetails.getUserId(), request));
    }

    // 예약 가능 시간 조회
    @GetMapping("/{doctorId}/schedules")
    public ApiResponse<List<DoctorScheduleResponse>> getAvailableSchedule(
            @PathVariable Long doctorId
    ) {
        return ApiResponse.success(
                doctorScheduleService.getAvailableSchedules(doctorId));
    }
}
