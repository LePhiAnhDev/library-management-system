package com.library.domain.report.dto;

import java.math.BigDecimal;

public record DashboardResponse(
        long totalBooks,
        long totalCopies,
        long totalMembers,
        long borrowedCount,
        long overdueCount,
        long pendingReservations,
        BigDecimal finesCollectedThisMonth
) {
}
