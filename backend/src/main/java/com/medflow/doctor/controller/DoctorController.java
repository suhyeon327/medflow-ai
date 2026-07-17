package com.medflow.doctor.controller;

import com.medflow.auth.security.CustomUserDetails;
import com.medflow.common.response.ApiResponse;
import com.medflow.doctor.dto.request.DoctorApplyRequest;
import com.medflow.doctor.dto.request.DoctorUpdateRequest;
import com.medflow.doctor.dto.response.DoctorApplyResponse;
import com.medflow.doctor.dto.response.DoctorDeleteResponse;
import com.medflow.doctor.dto.response.DoctorInfoResponse;
import com.medflow.doctor.dto.response.DoctorUpdateResponse;
import com.medflow.doctor.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/doctor")
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping("/apply")
    public ApiResponse<DoctorApplyResponse> apply(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody DoctorApplyRequest request
    ) {
        return ApiResponse.success(
                doctorService.apply(userDetails.getUserId(), request)
        );
    }

    // 의사 프로필 조회
    @GetMapping("/profile")
    public ApiResponse<DoctorInfoResponse> getMyDoctorInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        return ApiResponse.success(
                doctorService.getMyDoctorInfo(userDetails.getUserId())
        );
    }

    // 의사 정보 수정
    @PatchMapping("/profile")
    public ApiResponse<DoctorUpdateResponse> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody DoctorUpdateRequest request
    ) {
        return ApiResponse.success(
                doctorService.update(userDetails.getUserId(), request)
        );
    }

    // 의사 인증 신청 취소
    @DeleteMapping("/profile")
    public ApiResponse<DoctorDeleteResponse> cancel(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.success(
                doctorService.cancel(userDetails.getUserId()));
    }
}
