package com.library.domain.fine;

import com.library.domain.settings.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Lost book fine = the override amount when provided, otherwise the configured replacement fee.
 */
@Component
@RequiredArgsConstructor
public class LostFineStrategy implements FineCalculationStrategy {

    private final SettingsService settingsService;

    @Override
    public FineType type() {
        return FineType.LOST;
    }

    @Override
    public BigDecimal calculate(FineCalculationContext context) {
        return context.overrideAmount() != null
                ? context.overrideAmount()
                : settingsService.getLibrarySettings().getLostDefaultFee();
    }
}
