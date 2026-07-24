package com.library.common;

/**
 * Soft-delete lifecycle for reference entities (category, author, publisher).
 * Deleting sets INACTIVE so records referenced by history are never physically removed.
 */
public enum RecordStatus {
    ACTIVE,
    INACTIVE
}
