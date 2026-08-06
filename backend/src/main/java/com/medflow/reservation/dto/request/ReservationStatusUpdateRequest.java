package com.medflow.reservation.dto.request;

import com.medflow.reservation.entity.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ReservationStatusUpdateRequest(

        @NotNull
        @Schema(allowableValues = {"COMPLETED"})
        ReservationStatus status
) {
}
