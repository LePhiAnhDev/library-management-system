package com.library.domain.fine;

import java.math.BigDecimal;

/**
 * Strategy for computing a fine amount by type. Implementations are Spring beans selected by
 * FineStrategyFactory.
 */
public interface FineCalculationStrategy {

    FineType type();

    BigDecimal calculate(FineCalculationContext context);
}
