package com.medflow.doctor.controller;

import com.medflow.common.response.ApiResponse;
import com.medflow.doctor.dto.response.AvailableDoctorScheduleResponse;
import com.medflow.doctor.dto.response.DoctorDetailResponse;
import com.medflow.doctor.service.PublicDoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/doctors")
@Tag(name = "의사 공개 조회", description = "인증 없이 이용 가능한 의사 조회 API")
public class PublicDoctorController {

    private final PublicDoctorService publicDoctorService;

    @Operation(summary = "의사 상세 조회", description = "승인 완료되고 활성화된 의사만 반환합니다.", security = {})
    @GetMapping("/{doctorId}")
    public ApiResponse<DoctorDetailResponse> getDoctor(
            @PathVariable Long doctorId
    ) {
        return ApiResponse.success(publicDoctorService.getDoctor(doctorId));
    }

    @Operation(summary = "예약 가능 진료 스케줄 조회", description = "승인 완료되고 활성화된 의사의 예약 가능 스케줄을 반환합니다.", security = {})
    @GetMapping("/{doctorId}/available-schedules")
    public ApiResponse<List<AvailableDoctorScheduleResponse>> getAvailableDoctorSchedules(
            @PathVariable Long doctorId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.success(
                publicDoctorService.getAvailableDoctorSchedules(doctorId, date)
        );
    }
}
