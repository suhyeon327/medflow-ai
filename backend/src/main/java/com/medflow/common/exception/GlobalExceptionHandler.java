package com.medflow.common.exception;

import com.medflow.common.response.ApiResponse;
import com.medflow.common.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice   // 모든 Controller에서 발생한 예외를 가로챔
public class GlobalExceptionHandler {

    // 비즈니스 예외 처리
    @ExceptionHandler(BusinessException.class)   // 비즈니스 예외만 따로 처리
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {

        ErrorResponse error = ErrorResponse.builder()
                .code(e.getErrorCode().getCode())
                .message(e.getErrorCode().getMessage())
                .build();

        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ApiResponse.fail(error));
    }

    // 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {

        log.error("Unexpected Exception", e);
        ErrorResponse error = ErrorResponse.builder()
                .code("INTERNAL_SERVER_ERROR")
                .message("서버 오류가 발생했습니다.")
                .build();

        return ResponseEntity
                .internalServerError()   // 500 에러
                .body(ApiResponse.fail(error));
    }
}
