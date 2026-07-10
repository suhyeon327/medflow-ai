package com.medflow.auth.dto;

import com.medflow.user.entity.User;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class WithdrawResponse {

    @NotNull
    private Long id;

    @NotNull
    private LocalDateTime deleteAt;

    private String message;

    public static WithdrawResponse from(User user) {
        return WithdrawResponse.builder()
                .id(user.getId())
                .deleteAt(user.getDeletedAt())
                .message("회원 탈퇴가 완료되었습니다.")
                .build();
    }
}
