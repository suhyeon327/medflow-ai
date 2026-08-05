package com.medflow.user.controller;

import com.medflow.common.response.ApiResponse;
import com.medflow.user.dto.AdminUserResponse;
import com.medflow.user.dto.AdminUserPageResponse;
import com.medflow.user.entity.UserRole;
import com.medflow.user.entity.UserStatus;
import com.medflow.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final UserService userService;

    // 단일 회원 조회
    @GetMapping("/{userId}")
    public ApiResponse<AdminUserResponse> getUser(
            @PathVariable Long userId
    ) {
        return ApiResponse.success(
                userService.getUser(userId));
    }

    // 전체 회원 조회 및 필터링
    @GetMapping
    public ApiResponse<AdminUserPageResponse> getUsers(
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) UserStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponse.success(
                userService.getUsers(role, status, pageable));
    }
}
