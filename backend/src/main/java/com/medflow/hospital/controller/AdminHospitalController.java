package com.medflow.hospital.controller;

import com.medflow.common.response.ApiResponse;
import com.medflow.hospital.dto.response.AdminHospitalResponse;
import com.medflow.hospital.dto.request.AdminHospitalCreateRequest;
import com.medflow.hospital.dto.request.AdminHospitalUpdateRequest;
import com.medflow.hospital.dto.response.AdminHospitalDeleteResponse;
import com.medflow.hospital.service.AdminHospitalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/hospitals")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminHospitalController {

    private final AdminHospitalService adminHospitalService;

    // 병원 등록
    @PostMapping
    public ApiResponse<AdminHospitalResponse> createHospital(
            @Valid @RequestBody AdminHospitalCreateRequest request
            ) {
        return ApiResponse.success(
                adminHospitalService.createHospital(request)
        );
    }

    // 병원 관리 목록 조회
    @GetMapping
    public ApiResponse<List<AdminHospitalResponse>> getHospitals() {
        return ApiResponse.success(
                adminHospitalService.getHospitals()
        );
    }

    // 병원 정보 수정
    @PutMapping("/{hospitalId}")
    public ApiResponse<AdminHospitalResponse> updateHospital(
            @PathVariable Long hospitalId,
            @Valid @RequestBody AdminHospitalUpdateRequest request
    ) {
        return ApiResponse.success(
                adminHospitalService.updateHospital(hospitalId, request)
        );
    }

    // 병원 삭제
    @DeleteMapping("/{hospitalId}")
    public ApiResponse<AdminHospitalDeleteResponse> deleteHospital(
            @PathVariable Long hospitalId
    ) {
        return ApiResponse.success(
                adminHospitalService.deleteHospital(hospitalId)
        );
    }
}
