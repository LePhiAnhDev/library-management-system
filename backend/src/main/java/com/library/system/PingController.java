package com.library.system;

import com.library.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Authenticated connectivity check. Confirms the resource server accepted the Clerk token
 * and echoes the token subject. Used by the frontend and by the Phase 1 checkpoint.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "System", description = "Kiểm tra kết nối và xác thực")
public class PingController {

    @Operation(summary = "Ping có xác thực",
            description = "Trả về subject của token Clerk khi người dùng đã đăng nhập")
    @GetMapping("/ping")
    public ApiResponse<Map<String, Object>> ping(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "ok");
        data.put("sub", jwt.getSubject());
        data.put("time", Instant.now().toString());
        return ApiResponse.success(data, "pong");
    }
}
