package com.library.domain.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    /** Earliest waiting reservation for a title (FIFO by creation time). */
    Optional<Reservation> findFirstByBookIdAndStatusOrderByCreatedAtAsc(Long bookId, ReservationStatus status);

    boolean existsByBookIdAndMemberIdAndStatusIn(Long bookId, Long memberId, Collection<ReservationStatus> statuses);

    boolean existsByBookIdAndStatus(Long bookId, ReservationStatus status);

    Optional<Reservation> findByHeldCopyIdAndStatus(Long heldCopyId, ReservationStatus status);

    List<Reservation> findByStatusAndPickupExpiryBefore(ReservationStatus status, LocalDate date);

    List<Reservation> findByBookIdOrderByCreatedAtAsc(Long bookId);
}
