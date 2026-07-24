package com.library.domain.report;

import com.library.common.RecordStatus;
import com.library.domain.book.BookCopyRepository;
import com.library.domain.book.BookCopyStatus;
import com.library.domain.book.BookRepository;
import com.library.domain.fine.Fine;
import com.library.domain.fine.FineRepository;
import com.library.domain.fine.FineSpecifications;
import com.library.domain.fine.FineStatus;
import com.library.domain.loan.Loan;
import com.library.domain.loan.LoanRepository;
import com.library.domain.loan.LoanSpecifications;
import com.library.domain.loan.LoanStatus;
import com.library.domain.member.MemberRepository;
import com.library.domain.report.dto.ActiveMemberRow;
import com.library.domain.report.dto.DashboardResponse;
import com.library.domain.report.dto.FinesSummaryResponse;
import com.library.domain.report.dto.InventoryRow;
import com.library.domain.report.dto.LoanTrendPoint;
import com.library.domain.report.dto.TopBookRow;
import com.library.domain.reservation.ReservationRepository;
import com.library.domain.reservation.ReservationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final MemberRepository memberRepository;
    private final LoanRepository loanRepository;
    private final FineRepository fineRepository;
    private final ReservationRepository reservationRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse dashboard() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        Instant monthStart = today.withDayOfMonth(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endOfToday = today.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        return new DashboardResponse(
                bookRepository.countByStatus(RecordStatus.ACTIVE),
                bookCopyRepository.count(),
                memberRepository.count(),
                loanRepository.countByStatus(LoanStatus.BORROWED),
                loanRepository.countOverdue(today),
                reservationRepository.countByStatus(ReservationStatus.PENDING),
                fineRepository.sumByStatusAndPaidAtBetween(FineStatus.PAID, monthStart, endOfToday));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopBookRow> topBooks(LocalDate from, LocalDate to, int limit) {
        return loanRepository.topBorrowedBooks(from, to, PageRequest.of(0, limit)).stream()
                .map(row -> new TopBookRow((Long) row[0], (String) row[1], (Long) row[2]))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActiveMemberRow> activeMembers(LocalDate from, LocalDate to, int limit) {
        return loanRepository.mostActiveMembers(from, to, PageRequest.of(0, limit)).stream()
                .map(row -> new ActiveMemberRow((Long) row[0], (String) row[1], (String) row[2], (Long) row[3]))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanTrendPoint> loansOverTime(LocalDate from, LocalDate to) {
        return loanRepository.loansPerDay(from, to).stream()
                .map(row -> new LoanTrendPoint((LocalDate) row[0], (Long) row[1]))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryRow> inventory() {
        return bookCopyRepository.inventoryByStatus().stream()
                .map(row -> new InventoryRow(((BookCopyStatus) row[0]).name(), (Long) row[1]))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FinesSummaryResponse finesSummary(LocalDate from, LocalDate to) {
        Instant fromInstant = from.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant toInstant = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        return new FinesSummaryResponse(
                fineRepository.sumByStatusAndPaidAtBetween(FineStatus.PAID, fromInstant, toInstant),
                fineRepository.sumByStatusAndPaidAtBetween(FineStatus.WAIVED, fromInstant, toInstant),
                fineRepository.sumByStatus(FineStatus.UNPAID));
    }

    @Override
    @Transactional(readOnly = true)
    public String exportLoansCsv(LocalDate from, LocalDate to) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        List<Loan> loans = loanRepository.findAll(
                Specification.allOf(LoanSpecifications.borrowedFrom(from), LoanSpecifications.borrowedTo(to)),
                Sort.by(Sort.Direction.ASC, "borrowDate"));
        StringBuilder sb = new StringBuilder("﻿");
        sb.append(row("Mã phiếu", "Mã độc giả", "Tên độc giả", "Mã vạch", "Tên sách",
                "Ngày mượn", "Hạn trả", "Ngày trả", "Trạng thái", "Số lần gia hạn"));
        for (Loan loan : loans) {
            String status = loan.getStatus() == LoanStatus.BORROWED && loan.getDueDate().isBefore(today)
                    ? "OVERDUE" : loan.getStatus().name();
            sb.append(row(loan.getCode(), loan.getMember().getMemberCode(), loan.getMember().getFullName(),
                    loan.getBookCopy().getBarcode(), loan.getBookCopy().getBook().getTitle(),
                    text(loan.getBorrowDate()), text(loan.getDueDate()), text(loan.getReturnDate()),
                    status, String.valueOf(loan.getRenewCount())));
        }
        return sb.toString();
    }

    @Override
    @Transactional(readOnly = true)
    public String exportFinesCsv(LocalDate from, LocalDate to) {
        List<Fine> fines = fineRepository.findAll(
                Specification.allOf(FineSpecifications.createdFrom(from), FineSpecifications.createdTo(to)),
                Sort.by(Sort.Direction.ASC, "createdAt"));
        StringBuilder sb = new StringBuilder("﻿");
        sb.append(row("Mã", "Mã độc giả", "Tên độc giả", "Loại", "Số tiền", "Trạng thái", "Lý do", "Ngày tạo", "Ngày xử lý"));
        for (Fine fine : fines) {
            sb.append(row(String.valueOf(fine.getId()), fine.getMember().getMemberCode(), fine.getMember().getFullName(),
                    fine.getType().name(), text(fine.getAmount()), fine.getStatus().name(),
                    fine.getReason(), text(fine.getCreatedAt()), text(fine.getPaidAt())));
        }
        return sb.toString();
    }

    private static String row(String... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escape(fields[i]));
        }
        return sb.append('\n').toString();
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private static String text(Object value) {
        return value == null ? "" : value.toString();
    }
}
