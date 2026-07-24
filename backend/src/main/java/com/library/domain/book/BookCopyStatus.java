package com.library.domain.book;

/**
 * Physical state of a single book copy.
 * AVAILABLE copies can be borrowed; RESERVED is held for a ready reservation; BORROWED is on loan;
 * LOST / DAMAGED / MAINTENANCE are out of circulation.
 */
public enum BookCopyStatus {
    AVAILABLE,
    BORROWED,
    RESERVED,
    LOST,
    DAMAGED,
    MAINTENANCE
}
