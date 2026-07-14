package com.medflow.hospital.controller;

import com.medflow.common.response.ApiResponse;
import com.medflow.hospital.dto.HospitalRequest;
import com.medflow.hospital.dto.HospitalResponse;
import com.medflow.hospital.service.HospitalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
