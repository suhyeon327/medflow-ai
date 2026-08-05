package com.medflow.hospital.dto.response;

import com.medflow.hospital.entity.Hospital;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class deleteResponse {

    @NotNull
    private Long hospitalId;

    @NotNull
    private LocalDateTime deleteAt;

    private String message;

    public static deleteResponse from(Hospital hospital) {
        return deleteResponse.builder()
                .hospitalId(hospital.getId())
                .deleteAt(hospital.getDeletedAt())
                .message("병원 삭제가 완료되었습니다.")
                .build();
    }
}
