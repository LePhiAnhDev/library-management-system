package com.library.domain.fine.dto;

import com.library.domain.fine.FineType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Manual fine creation payload (a standalone penalty not tied to a return).
 */
public record FineCreateRequest(

        @NotNull(message = "Độc giả không được để trống")
        Long memberId,

        @NotNull(message = "Loại phạt không được để trống")
        FineType type,

        @NotNull(message = "Số tiền không được để trống")
        @Positive(message = "Số tiền phải lớn hơn 0")
        BigDecimal amount,

        @Size(max = 500, message = "Lý do tối đa 500 ký tự")
        String reason
) {
}
