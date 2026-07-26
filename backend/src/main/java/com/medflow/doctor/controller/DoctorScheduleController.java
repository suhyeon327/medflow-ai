package com.medflow.doctor.controller;

import com.medflow.auth.security.CustomUserDetails;
import com.medflow.common.response.ApiResponse;
import com.medflow.doctor.dto.request.DoctorScheduleCreateRequest;
import com.medflow.doctor.dto.response.DoctorScheduleResponse;
import com.medflow.doctor.service.DoctorScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/doctors")
public class DoctorScheduleController {

    @PostMapping("/schedules")
    public ApiResponse<List<DoctorScheduleResponse>> createDoctorSchedules(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody DoctorScheduleCreateRequest request
    ) {
        log.info("로그인 사용자 ID : {}", userDetails.getUserId());

        return ApiResponse.success(
                doctorScheduleService.createSchedule(userDetails.getUserId(), request));
    }

    private final DoctorScheduleService doctorScheduleService;

    @GetMapping("/{doctorId}/schedules")
    public ApiResponse<List<DoctorScheduleResponse>> getAvailableSchedule(
            @PathVariable Long doctorId
    ) {
        return ApiResponse.success(
                doctorScheduleService.getAvailableSchedules(doctorId));
    }
}
