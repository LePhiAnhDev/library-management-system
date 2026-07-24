package com.library.domain.report;

import com.library.common.ApiResponse;
import com.library.domain.report.dto.ActiveMemberRow;
import com.library.domain.report.dto.DashboardResponse;
import com.library.domain.report.dto.FinesSummaryResponse;
import com.library.domain.report.dto.InventoryRow;
import com.library.domain.report.dto.LoanTrendPoint;
import com.library.domain.report.dto.TopBookRow;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Báo cáo và bảng điều khiển")
public class ReportController {

    private final ReportService service;

    @Operation(summary = "Số liệu tổng quan bảng điều khiển")
    @GetMapping("/dashboard")
    public ApiResponse<DashboardResponse> dashboard() {
        return ApiResponse.success(service.dashboard());
    }

    @Operation(summary = "Sách được mượn nhiều nhất")
    @GetMapping("/top-books")
    public ApiResponse<List<TopBookRow>> topBooks(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.success(service.topBooks(from(from), to(to), limit));
    }

    @Operation(summary = "Độc giả tích cực nhất")
    @GetMapping("/active-members")
    public ApiResponse<List<ActiveMemberRow>> activeMembers(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.success(service.activeMembers(from(from), to(to), limit));
    }

    @Operation(summary = "Lượt mượn theo ngày")
    @GetMapping("/loans-over-time")
    public ApiResponse<List<LoanTrendPoint>> loansOverTime(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponse.success(service.loansOverTime(from(from), to(to)));
    }

    @Operation(summary = "Tồn kho sách theo tình trạng bản sao")
    @GetMapping("/inventory")
    public ApiResponse<List<InventoryRow>> inventory() {
        return ApiResponse.success(service.inventory());
    }

    @Operation(summary = "Tổng hợp phạt theo kỳ")
    @GetMapping("/fines-summary")
    public ApiResponse<FinesSummaryResponse> finesSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponse.success(service.finesSummary(from(from), to(to)));
    }

    @Operation(summary = "Xuất CSV phiếu mượn theo kỳ")
    @GetMapping("/export/loans")
    public ResponseEntity<byte[]> exportLoans(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return csv(service.exportLoansCsv(from(from), to(to)), "loans.csv");
    }

    @Operation(summary = "Xuất CSV phạt theo kỳ")
    @GetMapping("/export/fines")
    public ResponseEntity<byte[]> exportFines(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return csv(service.exportFinesCsv(from(from), to(to)), "fines.csv");
    }

    private ResponseEntity<byte[]> csv(String content, String filename) {
        byte[] body = content.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(body);
    }

    private LocalDate from(LocalDate from) {
        return from != null ? from : LocalDate.now(ZoneOffset.UTC).minusDays(30);
    }

    private LocalDate to(LocalDate to) {
        return to != null ? to : LocalDate.now(ZoneOffset.UTC);
    }
}
