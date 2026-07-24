package com.library.domain.settings.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record SettingsResponse(
        String libraryName,
        String libraryAddress,
        BigDecimal overdueFinePerDay,
        BigDecimal fineBlockThreshold,
        int reservationHoldDays,
        BigDecimal lostDefaultFee,
        BigDecimal damagedDefaultFee,
        List<LoanPolicyResponse> loanPolicies,
        Instant updatedAt
) {
}
