package com.library.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Uniform response envelope returned by every endpoint in the system.
 * Null fields are omitted, so errorCode appears only on failures and data only when present.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;
    private final String errorCode;
    private final Instant timestamp;

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "OK");
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String message, String errorCode, T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }
}
