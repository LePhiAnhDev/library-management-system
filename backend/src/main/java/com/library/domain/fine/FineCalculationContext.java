package com.library.domain.fine;

import java.math.BigDecimal;

/**
 * Inputs to a fine calculation. overdueDays applies to OVERDUE; overrideAmount lets staff override
 * the configured default fee for LOST / DAMAGED at return time (null means use the configured default).
 */
public record FineCalculationContext(long overdueDays, BigDecimal overrideAmount) {
}
