package com.library.exception;

/**
 * Thrown when creating or updating a resource would break a uniqueness rule
 * (for example a duplicate ISBN, barcode, member code or email). Maps to HTTP 409.
 */
public class DuplicateResourceException extends ApiException {

    public DuplicateResourceException(String message) {
        super(ErrorCode.DUPLICATE_RESOURCE, message);
    }
}
