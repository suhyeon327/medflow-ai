package com.medflow.auth.dto.response;

import com.medflow.user.entity.User;

import java.time.LocalDateTime;

public record WithdrawResponse(
        Long id,
        LocalDateTime deleteAt,
        String message
) {
    public static WithdrawResponse from(User user) {
        return new WithdrawResponse(
                user.getId(),
                user.getDeletedAt(),
                "회원 탈퇴가 완료되었습니다."
        );
    }
}
