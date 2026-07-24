package com.library.domain.fine;

import com.library.domain.loan.Loan;
import com.library.domain.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Creates fines via the strategy factory and reports outstanding balances.
 * Settlement (pay / waive) and listing are added with the fine management module.
 */
@Service
@RequiredArgsConstructor
public class FineService {

    private final FineRepository fineRepository;
    private final FineStrategyFactory strategyFactory;

    @Transactional
    public Fine createOverdueFine(Member member, Loan loan, long overdueDays) {
        return create(member, loan, FineType.OVERDUE, new FineCalculationContext(overdueDays, null),
                "Phạt quá hạn " + overdueDays + " ngày");
    }

    @Transactional
    public Fine createLostFine(Member member, Loan loan, BigDecimal overrideAmount) {
        return create(member, loan, FineType.LOST, new FineCalculationContext(0, overrideAmount),
                "Phạt làm mất sách");
    }

    @Transactional
    public Fine createDamagedFine(Member member, Loan loan, BigDecimal overrideAmount) {
        return create(member, loan, FineType.DAMAGED, new FineCalculationContext(0, overrideAmount),
                "Phạt làm hỏng sách");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalUnpaid(Long memberId) {
        return fineRepository.sumByMemberAndStatus(memberId, FineStatus.UNPAID);
    }

    private Fine create(Member member, Loan loan, FineType type, FineCalculationContext context, String reason) {
        BigDecimal amount = strategyFactory.get(type).calculate(context);
        Fine fine = Fine.builder()
                .member(member)
                .loan(loan)
                .type(type)
                .amount(amount)
                .status(FineStatus.UNPAID)
                .reason(reason)
                .build();
        return fineRepository.save(fine);
    }
}
