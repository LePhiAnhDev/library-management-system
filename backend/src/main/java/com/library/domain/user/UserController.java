package com.library.domain.user;

import com.library.common.ApiResponse;
import com.library.domain.user.dto.UserResponse;
import com.library.security.CurrentUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the current staff member's internal profile. There is no user management surface,
 * no roles and no admin: this endpoint only reflects who is logged in.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Hồ sơ nhân viên đang đăng nhập")
public class UserController {

    private final CurrentUserService currentUserService;
    private final UserMapper userMapper;

    @Operation(summary = "Hồ sơ của tôi", description = "Trả về hồ sơ nội bộ của nhân viên hiện tại")
    @GetMapping("/me")
    public ApiResponse<UserResponse> me() {
        return ApiResponse.success(userMapper.toResponse(currentUserService.getCurrentUser()));
    }
}
