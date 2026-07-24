package com.library.domain.loan.dto;

import com.library.domain.loan.CopyReturnCondition;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

/**
 * Return a copy by scanning its barcode; the active loan is located automatically.
 */
public record ReturnByBarcodeRequest(

        @NotBlank(message = "Mã vạch không được để trống")
        String barcode,

        CopyReturnCondition condition,
        String note,
        BigDecimal overrideFee
) {
}
