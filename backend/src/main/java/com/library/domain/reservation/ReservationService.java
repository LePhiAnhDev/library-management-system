package com.library.domain.reservation;

import com.library.common.PageResponse;
import com.library.domain.book.BookCopy;
import com.library.domain.reservation.dto.ReservationCreateRequest;
import com.library.domain.reservation.dto.ReservationResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ReservationService {

    ReservationResponse create(ReservationCreateRequest request);

    ReservationResponse cancel(Long id);

    ReservationResponse getById(Long id);

    PageResponse<ReservationResponse> list(Long bookId, Long memberId, ReservationStatus status, Pageable pageable);

    List<ReservationResponse> queueForBook(Long bookId);

    /** Expires READY holds past their pickup deadline and passes the copy to the next in queue. */
    int expireStaleHolds();

    // Hooks used by the loan lifecycle.

    /** If a title has a waiting reservation, mark the earliest READY holding this copy. Returns true if held. */
    boolean holdForNextInQueue(BookCopy copy);

    boolean hasPendingReservation(Long bookId);

    Optional<Reservation> findActiveHold(Long copyId);

    void markFulfilled(Reservation reservation);
}
