package com.medflow.user.controller;

import com.medflow.common.response.ApiResponse;
import com.medflow.security.principal.UserPrincipal;
import com.medflow.user.dto.request.WithdrawRequest;
import com.medflow.user.dto.response.WithdrawResponse;
import com.medflow.user.service.UserAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class UserAccountController {

    private final UserAccountService userAccountService;

    // 회원 탈퇴
    @DeleteMapping("/withdraw")
    public ApiResponse<WithdrawResponse> withdraw(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody @Valid WithdrawRequest request
    ) {
        return ApiResponse.success(
                userAccountService.withdraw(userPrincipal.getUserId(), request)
        );
    }
}
