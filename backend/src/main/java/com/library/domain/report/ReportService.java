package com.library.domain.report;

import com.library.domain.report.dto.ActiveMemberRow;
import com.library.domain.report.dto.DashboardResponse;
import com.library.domain.report.dto.FinesSummaryResponse;
import com.library.domain.report.dto.InventoryRow;
import com.library.domain.report.dto.LoanTrendPoint;
import com.library.domain.report.dto.TopBookRow;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    DashboardResponse dashboard();

    List<TopBookRow> topBooks(LocalDate from, LocalDate to, int limit);

    List<ActiveMemberRow> activeMembers(LocalDate from, LocalDate to, int limit);

    List<LoanTrendPoint> loansOverTime(LocalDate from, LocalDate to);

    List<InventoryRow> inventory();

    FinesSummaryResponse finesSummary(LocalDate from, LocalDate to);

    String exportLoansCsv(LocalDate from, LocalDate to);

    String exportFinesCsv(LocalDate from, LocalDate to);
}
