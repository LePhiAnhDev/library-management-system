package com.library.domain.book.dto;

import java.time.Instant;
import java.time.LocalDate;

public record BookCopyResponse(
        Long id,
        Long bookId,
        String bookTitle,
        String barcode,
        String shelfLocation,
        String status,
        LocalDate acquiredDate,
        String conditionNote,
        Instant createdAt,
        Instant updatedAt
) {
}
