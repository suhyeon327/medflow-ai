package com.medflow.hospital.controller;

import com.medflow.common.response.ApiResponse;
import com.medflow.hospital.dto.HospitalResponse;
import com.medflow.hospital.service.HospitalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hospitals")
@RequiredArgsConstructor
public class HospitalController {

    private final HospitalService hospitalService;

    // 병원 목록 조회
    @GetMapping("/")
    public ApiResponse<List<HospitalResponse>> getHospitals() {
        return ApiResponse.success(
                hospitalService.getAvailableHospitals()
        );
    }
}
