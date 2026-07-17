package com.medflow.doctor.controller;

import com.medflow.auth.security.CustomUserDetails;
import com.medflow.common.response.ApiResponse;
import com.medflow.doctor.dto.request.DoctorApplyRequest;
import com.medflow.doctor.dto.response.DoctorApplyResponse;
import com.medflow.doctor.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.attribute.UserPrincipal;

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
}
