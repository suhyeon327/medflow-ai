package com.medflow.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)   // 널 값의 속성은 포함하지 않음
public record ApiResponse<T> (
        boolean success,
        T data,
        ErrorResponse error,
        LocalDateTime timestamp
) {

    // 성공
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                true,
                data,
                null,
                LocalDateTime.now()
        );
    }

    // 실패
    public static <T> ApiResponse<T> fail(ErrorResponse error) {
        return new ApiResponse<>(
                false,
                null,
                error,
                LocalDateTime.now()
        );
    }
}