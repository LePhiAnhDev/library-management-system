package com.library.domain.loan.dto;

import java.time.Instant;
import java.time.LocalDate;

public record LoanResponse(
        Long id,
        String code,
        Long memberId,
        String memberCode,
        String memberName,
        Long bookCopyId,
        String barcode,
        Long bookId,
        String bookTitle,
        LocalDate borrowDate,
        LocalDate dueDate,
        LocalDate returnDate,
        String status,
        boolean overdue,
        int renewCount,
        Long createdById,
        String createdByName,
        Long returnedById,
        String returnedByName,
        Instant createdAt
) {
}
