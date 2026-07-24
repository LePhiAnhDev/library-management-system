package com.library.domain.fine;

/**
 * Reason a fine was raised. Each type has its own calculation strategy.
 */
public enum FineType {
    OVERDUE,
    LOST,
    DAMAGED
}
