package com.medflow.user.controller;

import com.medflow.common.response.ApiResponse;
import com.medflow.user.dto.AdminUserResponse;
import com.medflow.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ApiResponse<AdminUserResponse> getUser(
            @PathVariable Long id
    ) {
        return ApiResponse.success(
                userService.getUser(id));
    }

    // 전체 회원 조회
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/")
    public ApiResponse<List<AdminUserResponse>> getUsers() {
        return ApiResponse.success(
                userService.getUsers());
    }
}
