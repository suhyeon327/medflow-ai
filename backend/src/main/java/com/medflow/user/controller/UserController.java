package com.medflow.user.controller;

import com.medflow.common.response.ApiResponse;
import com.medflow.user.dto.UserResponse;
import com.medflow.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 단일 회원 조회
    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUser(
            @PathVariable Long id
    ) {
        return ApiResponse.success(
                userService.getUser(id));
    }

    // 전체 회원 조회
    @GetMapping("/")
    public ApiResponse<List<UserResponse>> getUsers() {
        return ApiResponse.success(
                userService.getUsers());
    }
}
