package com.library.domain.report.dto;

import java.time.LocalDate;

public record LoanTrendPoint(LocalDate date, long count) {
}
