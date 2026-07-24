package com.library.domain.fine;

import com.library.domain.settings.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Damaged book fine = the override amount when provided, otherwise the configured damaged fee.
 */
@Component
@RequiredArgsConstructor
public class DamagedFineStrategy implements FineCalculationStrategy {

    private final SettingsService settingsService;

    @Override
    public FineType type() {
        return FineType.DAMAGED;
    }

    @Override
    public BigDecimal calculate(FineCalculationContext context) {
        return context.overrideAmount() != null
                ? context.overrideAmount()
                : settingsService.getLibrarySettings().getDamagedDefaultFee();
    }
}
