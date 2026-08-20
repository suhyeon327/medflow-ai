package com.medflow.hospital.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record HospitalPageResponse(
        List<HospitalListResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    public static HospitalPageResponse from(Page<HospitalListResponse> page) {
        return new HospitalPageResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
