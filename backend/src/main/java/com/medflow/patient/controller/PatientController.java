package com.medflow.patient.controller;

import com.medflow.auth.security.CustomUserDetails;
import com.medflow.common.response.ApiResponse;
import com.medflow.patient.dto.AdminPatientResponse;
import com.medflow.patient.dto.PatientRequest;
import com.medflow.patient.dto.PatientResponse;
import com.medflow.patient.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patient")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    // 환자 생성
    @PostMapping("/create")
    public ApiResponse<PatientResponse> createPatient(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PatientRequest request
            ) {
        return ApiResponse.success(
                patientService.createPatient(
                        userDetails.getUserId(),
                        request)
        );
    }

    // 환자(나) 프로필 조회
    @GetMapping("/profile")
    public ApiResponse<PatientResponse> getPatientProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.success(
                patientService.getPatientProfile(
                        userDetails.getUserId()
                )
        );
    }

    // 환자 정보 수정
    @PutMapping("/update")
    public ApiResponse<PatientResponse> updatePatient(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PatientRequest request
    ) {
        return ApiResponse.success(
                patientService.updatePatient(
                        userDetails.getUserId(),
                        request
                )
        );
    }

    // 전체 환자 조회
    @GetMapping("/")
    public ApiResponse<List<AdminPatientResponse>> getPatients() {
        return ApiResponse.success(
                patientService.getPatients());
    }
}
