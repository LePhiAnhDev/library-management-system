package com.library.domain.member.dto;

import com.library.domain.fine.dto.FineResponse;
import com.library.domain.loan.dto.LoanResponse;
import com.library.domain.reservation.dto.ReservationResponse;

import java.math.BigDecimal;
import java.util.List;

/**
 * Aggregated reader profile: card info, what they are currently borrowing, unpaid fines and
 * active reservations.
 */
public record MemberProfileResponse(
        MemberResponse member,
        List<LoanResponse> currentLoans,
        List<FineResponse> unpaidFines,
        BigDecimal totalUnpaidFines,
        List<ReservationResponse> activeReservations
) {
}
