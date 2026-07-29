package com.medflow.reservation.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record AdminReservationPageResponse(
        List<AdminReservationResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static AdminReservationPageResponse from(Page<AdminReservationResponse> page) {
        return new AdminReservationPageResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
