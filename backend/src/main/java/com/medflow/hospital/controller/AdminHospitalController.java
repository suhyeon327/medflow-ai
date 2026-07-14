package com.medflow.hospital.controller;

import com.medflow.common.response.ApiResponse;
import com.medflow.hospital.dto.AdminHospitalResponse;
import com.medflow.hospital.dto.HospitalRequest;
import com.medflow.hospital.dto.HospitalResponse;
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
    public ApiResponse<HospitalResponse> createHospital(
            @Valid @RequestBody HospitalRequest request
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
}
