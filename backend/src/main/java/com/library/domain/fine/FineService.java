package com.library.domain.fine;

import com.library.common.PageResponse;
import com.library.domain.fine.dto.FineCreateRequest;
import com.library.domain.fine.dto.FineResponse;
import com.library.domain.loan.Loan;
import com.library.domain.member.Member;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface FineService {

    // Automatic fine creation used by the loan return flow.
    Fine createOverdueFine(Member member, Loan loan, long overdueDays);

    Fine createLostFine(Member member, Loan loan, BigDecimal overrideAmount);

    Fine createDamagedFine(Member member, Loan loan, BigDecimal overrideAmount);

    BigDecimal totalUnpaid(Long memberId);

    // Fine management.
    FineResponse createManual(FineCreateRequest request);

    FineResponse settle(Long fineId);

    FineResponse waive(Long fineId, String reason);

    FineResponse getById(Long fineId);

    PageResponse<FineResponse> list(Long memberId, FineType type, FineStatus status,
                                    LocalDate from, LocalDate to, Pageable pageable);
}
