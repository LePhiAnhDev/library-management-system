package com.library.domain.settings.dto;

public record LoanPolicyResponse(
        String membershipType,
        int maxBooks,
        int loanPeriodDays,
        int maxRenewals
) {
}
