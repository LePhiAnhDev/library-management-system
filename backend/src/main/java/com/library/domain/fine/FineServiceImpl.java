package com.library.domain.fine;

import com.library.common.PageResponse;
import com.library.domain.fine.dto.FineCreateRequest;
import com.library.domain.fine.dto.FineResponse;
import com.library.domain.loan.Loan;
import com.library.domain.member.Member;
import com.library.domain.member.MemberRepository;
import com.library.exception.ResourceNotFoundException;
import com.library.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FineServiceImpl implements FineService {

    private final FineRepository fineRepository;
    private final FineStrategyFactory strategyFactory;
    private final FineMapper mapper;
    private final MemberRepository memberRepository;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional
    public Fine createOverdueFine(Member member, Loan loan, long overdueDays) {
        return create(member, loan, FineType.OVERDUE, new FineCalculationContext(overdueDays, null),
                "Phạt quá hạn " + overdueDays + " ngày");
    }

    @Override
    @Transactional
    public Fine createLostFine(Member member, Loan loan, BigDecimal overrideAmount) {
        return create(member, loan, FineType.LOST, new FineCalculationContext(0, overrideAmount),
                "Phạt làm mất sách");
    }

    @Override
    @Transactional
    public Fine createDamagedFine(Member member, Loan loan, BigDecimal overrideAmount) {
        return create(member, loan, FineType.DAMAGED, new FineCalculationContext(0, overrideAmount),
                "Phạt làm hỏng sách");
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal totalUnpaid(Long memberId) {
        return fineRepository.sumByMemberAndStatus(memberId, FineStatus.UNPAID);
    }

    @Override
    @Transactional
    public FineResponse createManual(FineCreateRequest request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> ResourceNotFoundException.of("độc giả", request.memberId()));
        Fine fine = Fine.builder()
                .member(member)
                .type(request.type())
                .amount(request.amount())
                .status(FineStatus.UNPAID)
                .reason(request.reason())
                .build();
        return mapper.toResponse(fineRepository.save(fine));
    }

    @Override
    @Transactional
    public FineResponse settle(Long fineId) {
        Fine fine = getEntity(fineId);
        // Idempotent: settling an already settled fine is a no-op, so retries never double count.
        if (fine.getStatus() == FineStatus.UNPAID) {
            fine.setStatus(FineStatus.PAID);
            fine.setPaidAt(Instant.now());
            fine.setSettledBy(currentUserService.getCurrentUser());
            fineRepository.save(fine);
        }
        return mapper.toResponse(fine);
    }

    @Override
    @Transactional
    public FineResponse waive(Long fineId, String reason) {
        Fine fine = getEntity(fineId);
        if (fine.getStatus() == FineStatus.UNPAID) {
            fine.setStatus(FineStatus.WAIVED);
            fine.setPaidAt(Instant.now());
            fine.setSettledBy(currentUserService.getCurrentUser());
            if (StringUtils.hasText(reason)) {
                fine.setReason(reason);
            }
            fineRepository.save(fine);
        }
        return mapper.toResponse(fine);
    }

    @Override
    @Transactional(readOnly = true)
    public FineResponse getById(Long fineId) {
        return mapper.toResponse(getEntity(fineId));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FineResponse> list(Long memberId, FineType type, FineStatus status,
                                           LocalDate from, LocalDate to, Pageable pageable) {
        List<Specification<Fine>> specs = new ArrayList<>();
        if (memberId != null) {
            specs.add(FineSpecifications.hasMember(memberId));
        }
        if (type != null) {
            specs.add(FineSpecifications.hasType(type));
        }
        if (status != null) {
            specs.add(FineSpecifications.hasStatus(status));
        }
        if (from != null) {
            specs.add(FineSpecifications.createdFrom(from));
        }
        if (to != null) {
            specs.add(FineSpecifications.createdTo(to));
        }
        Page<FineResponse> page = fineRepository.findAll(Specification.allOf(specs), pageable).map(mapper::toResponse);
        return PageResponse.from(page);
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

    private Fine getEntity(Long fineId) {
        return fineRepository.findById(fineId)
                .orElseThrow(() -> ResourceNotFoundException.of("phạt", fineId));
    }
}
