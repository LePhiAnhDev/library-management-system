package com.library.domain.fine.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record FineResponse(
        Long id,
        Long memberId,
        String memberCode,
        String memberName,
        Long loanId,
        String loanCode,
        String type,
        BigDecimal amount,
        String status,
        String reason,
        Instant paidAt,
        Long settledById,
        String settledByName,
        Instant createdAt
) {
}
