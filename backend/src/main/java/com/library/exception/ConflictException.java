package com.library.exception;

/**
 * Thrown when the request conflicts with the current state of a resource
 * (for example a copy that is no longer available). Maps to HTTP 409.
 */
public class ConflictException extends ApiException {

    public ConflictException(String message) {
        super(ErrorCode.CONFLICT, message);
    }
}
