package com.library.storage;

/**
 * Raised when an object storage operation fails. Handled as a 500 (INTERNAL_ERROR) with a stack trace logged.
 */
public class StorageException extends RuntimeException {

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
