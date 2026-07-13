package com.medflow.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medflow.common.exception.ErrorCode;
import com.medflow.common.response.ApiResponse;
import com.medflow.common.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;


    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {


        ErrorResponse error = ErrorResponse.builder()
                .code(ErrorCode.AUTH_UNAUTHORIZED.getCode())
                .message(ErrorCode.AUTH_UNAUTHORIZED.getMessage())
                .build();


        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");


        response.getWriter()
                .write(
                        objectMapper.writeValueAsString(
                                ApiResponse.fail(error)
                        )
                );
    }
}
