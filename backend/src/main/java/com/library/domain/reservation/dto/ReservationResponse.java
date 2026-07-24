package com.library.domain.reservation.dto;

import java.time.Instant;
import java.time.LocalDate;

public record ReservationResponse(
        Long id,
        Long memberId,
        String memberCode,
        String memberName,
        Long bookId,
        String bookTitle,
        String status,
        Instant reservationDate,
        Instant readyAt,
        LocalDate pickupExpiry,
        Long heldCopyId,
        String heldCopyBarcode
) {
}
