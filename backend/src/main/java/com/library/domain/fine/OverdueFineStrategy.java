package com.library.domain.fine;

import com.library.domain.settings.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Overdue fine = days late multiplied by the configured daily rate.
 */
@Component
@RequiredArgsConstructor
public class OverdueFineStrategy implements FineCalculationStrategy {

    private final SettingsService settingsService;

    @Override
    public FineType type() {
        return FineType.OVERDUE;
    }

    @Override
    public BigDecimal calculate(FineCalculationContext context) {
        long days = Math.max(0, context.overdueDays());
        return settingsService.getLibrarySettings().getOverdueFinePerDay()
                .multiply(BigDecimal.valueOf(days));
    }
}
