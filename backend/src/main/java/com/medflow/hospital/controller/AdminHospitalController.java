package com.medflow.hospital.controller;

import com.medflow.common.response.ApiResponse;
import com.medflow.hospital.dto.response.AdminHospitalResponse;
import com.medflow.hospital.dto.request.HospitalCreateRequest;
import com.medflow.hospital.dto.response.HospitalDetailResponse;
import com.medflow.hospital.dto.request.HospitalUpdateRequest;
import com.medflow.hospital.dto.response.deleteResponse;
import com.medflow.hospital.service.HospitalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/hospitals")
@RequiredArgsConstructor
public class AdminHospitalController {

    private final HospitalService hospitalService;

    // 병원 등록
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/")
    public ApiResponse<HospitalDetailResponse> createHospital(
            @Valid @RequestBody HospitalCreateRequest request
            ) {
        return ApiResponse.success(
                hospitalService.createHospital(request)
        );
    }

    // 병원 관리 목록 조회
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/")
    public ApiResponse<List<AdminHospitalResponse>> getHospitals() {
        return ApiResponse.success(
                hospitalService.getHospitals()
        );
    }

    // 병원 정보 수정
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{hospitalId}")
    public ApiResponse<AdminHospitalResponse> updateHospital(
            @PathVariable Long hospitalId,
            @Valid @RequestBody HospitalUpdateRequest request
    ) {
        return ApiResponse.success(
                hospitalService.updateHospital(hospitalId, request)
        );
    }

    // 병원 삭제
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{hospitalId}")
    public ApiResponse<deleteResponse> deleteHospital(
            @PathVariable Long hospitalId
    ) {
        return ApiResponse.success(
                hospitalService.deleteHospital(hospitalId)
        );
    }
}
