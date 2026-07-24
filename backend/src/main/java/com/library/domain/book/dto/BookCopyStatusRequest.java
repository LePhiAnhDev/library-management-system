package com.library.domain.book.dto;

import com.library.domain.book.BookCopyStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Manual status change for a copy (AVAILABLE, LOST, DAMAGED, MAINTENANCE only).
 * BORROWED and RESERVED are driven by loans and reservations, not set manually.
 */
public record BookCopyStatusRequest(

        @NotNull(message = "Trạng thái không được để trống")
        BookCopyStatus status,

        @Size(max = 1000, message = "Ghi chú tình trạng tối đa 1000 ký tự")
        String conditionNote
) {
}
