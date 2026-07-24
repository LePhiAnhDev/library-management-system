package com.library.exception;

/**
 * One field level validation failure, returned as a list in the error response data
 * so the frontend can attach messages to the right form field.
 */
public record ValidationError(String field, String message) {
}
