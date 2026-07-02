package com.medflow.common.response;

import lombok.Builder;

@Builder
public record ErrorResponse(
        String code,
        String message
) {
}
