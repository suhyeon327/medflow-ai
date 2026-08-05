package com.medflow.doctor.controller;

import com.medflow.common.response.ApiResponse;
import com.medflow.doctor.dto.response.AdminDoctorApproveResponse;
import com.medflow.doctor.dto.response.AdminDoctorDetailResponse;
import com.medflow.doctor.dto.response.AdminDoctorRejectResponse;
import com.medflow.doctor.service.AdminDoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/doctors")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDoctorController {

    private final AdminDoctorService adminDoctorService;

    @GetMapping("/{doctorId}")
    public ApiResponse<AdminDoctorDetailResponse> getDoctorDetail(
            @PathVariable Long doctorId
    ) {
        return ApiResponse.success(adminDoctorService.getDoctorDetail(doctorId));
    }

    @PatchMapping("/{doctorId}/approve")
    public ApiResponse<AdminDoctorApproveResponse> approveDoctor(
            @PathVariable Long doctorId
    ) {
        return ApiResponse.success(adminDoctorService.approveDoctor(doctorId));
    }

    @PatchMapping("/{doctorId}/reject")
    public ApiResponse<AdminDoctorRejectResponse> rejectDoctor(
            @PathVariable Long doctorId
    ) {
        return ApiResponse.success(adminDoctorService.rejectDoctor(doctorId));
    }
}
