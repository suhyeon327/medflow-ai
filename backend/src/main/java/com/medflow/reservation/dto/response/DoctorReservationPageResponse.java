package com.medflow.reservation.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record DoctorReservationPageResponse(
        List<DoctorReservationResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static DoctorReservationPageResponse from(Page<DoctorReservationResponse> page) {
        return new DoctorReservationPageResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
