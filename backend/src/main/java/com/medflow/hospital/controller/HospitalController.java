package com.medflow.hospital.controller;

import com.medflow.common.response.ApiResponse;
import com.medflow.doctor.dto.response.PublicDoctorResponse;
import com.medflow.hospital.dto.response.HospitalDetailResponse;
import com.medflow.hospital.dto.response.HospitalListResponse;
import com.medflow.hospital.service.HospitalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hospitals")
@RequiredArgsConstructor
@Tag(name = "병원 공개 조회", description = "인증 없이 이용 가능한 병원 및 소속 의사 조회 API")
public class HospitalController {

    private final HospitalService hospitalService;

    // 병원 목록 조회
    @Operation(summary = "병원 목록 조회", security = {})
    @GetMapping
    public ApiResponse<List<HospitalListResponse>> getHospitals() {
        return ApiResponse.success(
                hospitalService.getAvailableHospitals()
        );
    }

    // 병원 상세 정보 조회
    @Operation(summary = "병원 상세 조회", security = {})
    @GetMapping("/{hospitalId}")
    public ApiResponse<HospitalDetailResponse> getDetailHospital(
            @PathVariable Long hospitalId
            ) {
        return ApiResponse.success(
                hospitalService.getDetailHospital(hospitalId)
        );
    }

    // 병원별 의사 목록 조회
    @Operation(summary = "병원별 의사 목록 조회", description = "승인 완료되고 활성화된 의사만 반환합니다.", security = {})
    @GetMapping("/{hospitalId}/doctors")
    public ApiResponse<List<PublicDoctorResponse>> getDoctors(
            @PathVariable Long hospitalId
    ) {
        return ApiResponse.success(
                hospitalService.getAvailableDoctors(hospitalId)
        );
    }
}
