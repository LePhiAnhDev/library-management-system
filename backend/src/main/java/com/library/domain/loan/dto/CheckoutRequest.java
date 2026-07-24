package com.library.domain.loan.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Checkout payload. Identify the copy by id or by scanned barcode (at least one is required).
 */
public record CheckoutRequest(

        @NotNull(message = "Độc giả không được để trống")
        Long memberId,

        Long bookCopyId,

        String barcode
) {
}
