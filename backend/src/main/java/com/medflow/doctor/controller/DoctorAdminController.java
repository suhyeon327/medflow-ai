package com.medflow.doctor.controller;

import com.medflow.common.response.ApiResponse;
import com.medflow.doctor.dto.response.DoctorApproveResponse;
import com.medflow.doctor.dto.response.DoctorDetailResponse;
import com.medflow.doctor.dto.response.DoctorRejectResponse;
import com.medflow.doctor.dto.response.DoctorStatusResponse;
import com.medflow.doctor.entity.DoctorStatus;
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

    // 의사 상태 조회
    @GetMapping("/")
    public ApiResponse<List<DoctorStatusResponse>> getDoctorsByStatus(
            @RequestParam DoctorStatus status
    ) {
        return ApiResponse.success(
                doctorAdminService.getDoctorsByStatus(status)
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

    // 의사 반려
    @PatchMapping("/{doctorId}/reject")
    public ApiResponse<DoctorRejectResponse> rejectDoctor(
            @PathVariable Long doctorId
    ) {
        return ApiResponse.success(
                doctorAdminService.rejectDoctor(doctorId)
        );
    }
}
