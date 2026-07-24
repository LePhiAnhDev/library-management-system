package com.library.exception;

/**
 * Thrown when a requested entity does not exist. Maps to HTTP 404.
 */
public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }

    public static ResourceNotFoundException of(String resourceName, Object id) {
        return new ResourceNotFoundException("Không tìm thấy " + resourceName + " với id " + id);
    }
}
