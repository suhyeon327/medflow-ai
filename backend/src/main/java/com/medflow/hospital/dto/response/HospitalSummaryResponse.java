package com.medflow.hospital.dto.response;

public record HospitalSummaryResponse(
        long hospitalCount,
        long doctorCount
) {
}
