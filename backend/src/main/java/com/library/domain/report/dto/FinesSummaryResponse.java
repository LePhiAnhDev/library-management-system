package com.library.domain.report.dto;

import java.math.BigDecimal;

public record FinesSummaryResponse(BigDecimal collected, BigDecimal waived, BigDecimal unpaidTotal) {
}
