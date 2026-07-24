package com.library.domain.reservation;

/**
 * Reservation lifecycle. PENDING waits in the queue; READY means a copy is held for pickup;
 * FULFILLED after the hold is borrowed; CANCELLED by staff; EXPIRED when not picked up in time.
 */
public enum ReservationStatus {
    PENDING,
    READY,
    FULFILLED,
    CANCELLED,
    EXPIRED
}
