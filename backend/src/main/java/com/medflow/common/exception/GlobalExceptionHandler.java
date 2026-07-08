package com.medflow.common.exception;

import com.medflow.common.response.ApiResponse;
import com.medflow.common.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 비즈니스 예외 처리
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {

        ErrorResponse error = ErrorResponse.builder()
                .code(e.getErrorCode().getCode())
                .message(e.getErrorCode().getMessage())
                .build();

        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ApiResponse.fail(error));
    }

    // @Valid 검증 실패 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        ErrorResponse error = ErrorResponse.builder()
                .code("VALIDATION_ERROR")
                .message(e.getBindingResult().getAllErrors().get(0).getDefaultMessage())
                .build();

        return ResponseEntity
                .badRequest()
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
                .internalServerError()
                .body(ApiResponse.fail(error));
    }
}
