package com.medflow.patient.controller;

import com.medflow.auth.security.CustomUserDetails;
import com.medflow.common.response.ApiResponse;
import com.medflow.patient.dto.PatientRequest;
import com.medflow.patient.dto.PatientResponse;
import com.medflow.patient.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasRole('PATIENT')")
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    // 환자 프로필 조회
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
    @PutMapping("/profile")
    public ApiResponse<PatientResponse> updatePatientProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PatientRequest request
    ) {
        return ApiResponse.success(
                patientService.updatePatientProfile(
                        userDetails.getUserId(),
                        request
                )
        );
    }
}
