package com.library.exception;

import org.springframework.http.HttpStatus;

/**
 * Stable, machine-readable error codes returned in the ApiResponse envelope.
 * The name() is the wire value; each carries the HTTP status it maps to.
 */
public enum ErrorCode {

    VALIDATION_ERROR(HttpStatus.UNPROCESSABLE_CONTENT),
    BAD_REQUEST(HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT),
    BUSINESS_RULE_VIOLATION(HttpStatus.BAD_REQUEST),
    CONFLICT(HttpStatus.CONFLICT),
    OPTIMISTIC_LOCK_CONFLICT(HttpStatus.CONFLICT),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED(HttpStatus.FORBIDDEN),
    RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
