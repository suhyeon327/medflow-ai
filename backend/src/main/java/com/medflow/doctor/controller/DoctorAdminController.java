package com.medflow.doctor.controller;

import com.medflow.common.response.ApiResponse;
import com.medflow.doctor.dto.response.DoctorApproveResponse;
import com.medflow.doctor.dto.response.DoctorDetailResponse;
import com.medflow.doctor.dto.response.PendingDoctorResponse;
import com.medflow.doctor.service.DoctorAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/doctors")
@PreAuthorize("hasRole('ADMIN')")
public class DoctorAdminController {

    private final DoctorAdminService doctorAdminService;

    // 승인 대기 의사 목록 조회
    @GetMapping("/pending")
    public ApiResponse<List<PendingDoctorResponse>> getPendingDoctors() {

        return ApiResponse.success(
                doctorAdminService.getPendingDoctors()
        );
    }

    // 의사 인증 신청 상세 조회
    @GetMapping("/{doctorId}")
    public ApiResponse<DoctorDetailResponse> getDoctorDetail(
            @PathVariable Long doctorId
    ) {
        return ApiResponse.success(
                doctorAdminService.getDoctorDetail(doctorId)
        );
    }

    // 의사 승인
    @PatchMapping("/{doctorId}/approve")
    public ApiResponse<DoctorApproveResponse> approveDoctor(
            @PathVariable Long doctorId
    ) {
        return ApiResponse.success(
                doctorAdminService.approveDoctor(doctorId)
        );
    }
}
