package com.library.domain.fine;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Selects a fine calculation strategy by type (Factory pattern over the Strategy beans).
 */
@Component
public class FineStrategyFactory {

    private final Map<FineType, FineCalculationStrategy> strategies;

    public FineStrategyFactory(List<FineCalculationStrategy> strategyBeans) {
        this.strategies = strategyBeans.stream()
                .collect(Collectors.toMap(FineCalculationStrategy::type, Function.identity()));
    }

    public FineCalculationStrategy get(FineType type) {
        FineCalculationStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("Không có chiến lược tính phạt cho loại " + type);
        }
        return strategy;
    }
}
