package com.medflow.doctor.controller;

import com.medflow.common.response.ApiResponse;
import com.medflow.doctor.dto.response.AdminDoctorApproveResponse;
import com.medflow.doctor.dto.response.AdminDoctorDetailResponse;
import com.medflow.doctor.dto.response.AdminDoctorListResponse;
import com.medflow.doctor.dto.response.AdminDoctorRejectResponse;
import com.medflow.doctor.entity.DoctorStatus;
import com.medflow.doctor.service.AdminDoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/doctors")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDoctorController {

    private final AdminDoctorService adminDoctorService;

    // 의사 상세 조회
    @GetMapping("/{doctorId}")
    public ApiResponse<AdminDoctorDetailResponse> getDoctorDetail(
            @PathVariable Long doctorId
    ) {
        return ApiResponse.success(adminDoctorService.getDoctorDetail(doctorId));
    }

    // 의사 승인 대기 목록 조회
    @GetMapping
    public ApiResponse<List<AdminDoctorListResponse>> getDoctors(
            @RequestParam(required = false) DoctorStatus status
    ) {
        return ApiResponse.success(
                adminDoctorService.getDoctors(status)
        );
    }

    // 의사 승인
    @PatchMapping("/{doctorId}/approve")
    public ApiResponse<AdminDoctorApproveResponse> approveDoctor(
            @PathVariable Long doctorId
    ) {
        return ApiResponse.success(adminDoctorService.approveDoctor(doctorId));
    }

    // 의사 반려
    @PatchMapping("/{doctorId}/reject")
    public ApiResponse<AdminDoctorRejectResponse> rejectDoctor(
            @PathVariable Long doctorId
    ) {
        return ApiResponse.success(adminDoctorService.rejectDoctor(doctorId));
    }
}
