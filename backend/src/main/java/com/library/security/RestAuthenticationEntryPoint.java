package com.library.security;

import com.library.common.ApiResponse;
import com.library.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

/**
 * Returns the standard ApiResponse envelope with a 401 when a request is unauthenticated,
 * instead of the default empty Bearer challenge, so the frontend gets a consistent shape.
 * Uses the Jackson 3 JsonMapper that Spring Boot 4 autoconfigures.
 */
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final JsonMapper jsonMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ApiResponse<Object> body = ApiResponse.error(
                "Bạn cần đăng nhập để truy cập tài nguyên này", ErrorCode.UNAUTHORIZED.name());
        jsonMapper.writeValue(response.getWriter(), body);
    }
}
