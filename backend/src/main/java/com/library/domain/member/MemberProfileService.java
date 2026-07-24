package com.library.domain.member;

import com.library.domain.fine.FineService;
import com.library.domain.fine.FineStatus;
import com.library.domain.fine.dto.FineResponse;
import com.library.domain.loan.LoanService;
import com.library.domain.loan.LoanStatus;
import com.library.domain.loan.dto.LoanResponse;
import com.library.domain.member.dto.MemberProfileResponse;
import com.library.domain.reservation.ReservationService;
import com.library.domain.reservation.dto.ReservationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Aggregates the reader profile from the loan, fine and reservation modules.
 */
@Service
@RequiredArgsConstructor
public class MemberProfileService {

    private static final Set<String> ACTIVE_RESERVATION_STATUSES = Set.of("PENDING", "READY");
    private static final PageRequest PROFILE_PAGE = PageRequest.of(0, 200);

    private final MemberService memberService;
    private final LoanService loanService;
    private final FineService fineService;
    private final ReservationService reservationService;

    @Transactional(readOnly = true)
    public MemberProfileResponse profile(Long memberId) {
        List<LoanResponse> currentLoans = loanService
                .list(null, memberId, LoanStatus.BORROWED, null, null, PROFILE_PAGE).getContent();
        List<FineResponse> unpaidFines = fineService
                .list(memberId, null, FineStatus.UNPAID, null, null, PROFILE_PAGE).getContent();
        BigDecimal totalUnpaid = fineService.totalUnpaid(memberId);
        List<ReservationResponse> activeReservations = reservationService
                .list(null, memberId, null, PROFILE_PAGE).getContent().stream()
                .filter(reservation -> ACTIVE_RESERVATION_STATUSES.contains(reservation.status()))
                .toList();
        return new MemberProfileResponse(
                memberService.getById(memberId),
                currentLoans,
                unpaidFines,
                totalUnpaid,
                activeReservations);
    }
}
