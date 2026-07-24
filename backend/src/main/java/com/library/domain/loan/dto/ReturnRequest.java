package com.library.domain.loan.dto;

import com.library.domain.loan.CopyReturnCondition;

import java.math.BigDecimal;

/**
 * Return payload. condition defaults to NORMAL; overrideFee optionally overrides the default
 * lost/damaged fee.
 */
public record ReturnRequest(
        CopyReturnCondition condition,
        String note,
        BigDecimal overrideFee
) {
}
