package com.medflow.doctor.controller;

import com.medflow.common.response.ApiResponse;
import com.medflow.doctor.dto.response.DoctorResponse;
import com.medflow.doctor.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/doctors")
@Tag(name = "의사 공개 조회", description = "인증 없이 이용 가능한 의사 조회 API")
public class PublicDoctorController {

    private final DoctorService doctorService;

    // 공개 의사 상세 조회
    @Operation(summary = "의사 상세 조회", description = "승인 완료되고 활성화된 의사만 반환합니다.", security = {})
    @GetMapping("/{doctorId}")
    public ApiResponse<DoctorResponse> getDoctor(
            @PathVariable Long doctorId
    ) {
        return ApiResponse.success(
                doctorService.getPublicDoctor(doctorId)
        );
    }
}
