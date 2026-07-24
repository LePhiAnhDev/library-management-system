package com.library.exception;

import lombok.Getter;

/**
 * Base type for all domain exceptions. Carries a stable ErrorCode so the global
 * handler can map to the correct HTTP status and wire error code without leaking internals.
 */
@Getter
public class ApiException extends RuntimeException {

    private final transient ErrorCode errorCode;

    public ApiException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
