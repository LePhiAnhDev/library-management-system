package com.library.domain.reservation;

import com.library.common.PageResponse;
import com.library.domain.book.Book;
import com.library.domain.book.BookCopy;
import com.library.domain.book.BookCopyRepository;
import com.library.domain.book.BookCopyStatus;
import com.library.domain.book.BookCountService;
import com.library.domain.book.BookRepository;
import com.library.domain.member.Member;
import com.library.domain.member.MemberRepository;
import com.library.domain.member.MemberStatus;
import com.library.domain.reservation.dto.ReservationCreateRequest;
import com.library.domain.reservation.dto.ReservationResponse;
import com.library.domain.settings.SettingsService;
import com.library.exception.BusinessRuleException;
import com.library.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private static final List<ReservationStatus> ACTIVE_STATUSES = List.of(ReservationStatus.PENDING, ReservationStatus.READY);

    private final ReservationRepository reservationRepository;
    private final ReservationMapper mapper;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final BookCountService bookCountService;
    private final SettingsService settingsService;

    @Override
    @Transactional
    public ReservationResponse create(ReservationCreateRequest request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> ResourceNotFoundException.of("độc giả", request.memberId()));
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessRuleException("Độc giả không ở trạng thái hoạt động");
        }
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> ResourceNotFoundException.of("sách", request.bookId()));
        if (book.getAvailableCopies() > 0) {
            throw new BusinessRuleException("Sách vẫn còn bản sao, có thể mượn trực tiếp");
        }
        if (reservationRepository.existsByBookIdAndMemberIdAndStatusIn(book.getId(), member.getId(), ACTIVE_STATUSES)) {
            throw new BusinessRuleException("Độc giả đã đặt trước sách này");
        }
        Reservation reservation = Reservation.builder()
                .member(member)
                .book(book)
                .status(ReservationStatus.PENDING)
                .build();
        return mapper.toResponse(reservationRepository.save(reservation));
    }

    @Override
    @Transactional
    public ReservationResponse cancel(Long id) {
        Reservation reservation = getEntity(id);
        if (reservation.getStatus() != ReservationStatus.PENDING && reservation.getStatus() != ReservationStatus.READY) {
            throw new BusinessRuleException("Không thể hủy đặt trước ở trạng thái hiện tại");
        }
        boolean wasReady = reservation.getStatus() == ReservationStatus.READY;
        BookCopy heldCopy = reservation.getHeldCopy();
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setHeldCopy(null);
        reservationRepository.save(reservation);
        if (wasReady && heldCopy != null) {
            releaseHold(heldCopy);
        }
        return mapper.toResponse(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getById(Long id) {
        return mapper.toResponse(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReservationResponse> list(Long bookId, Long memberId, ReservationStatus status, Pageable pageable) {
        List<Specification<Reservation>> specs = new ArrayList<>();
        if (bookId != null) {
            specs.add(ReservationSpecifications.hasBook(bookId));
        }
        if (memberId != null) {
            specs.add(ReservationSpecifications.hasMember(memberId));
        }
        if (status != null) {
            specs.add(ReservationSpecifications.hasStatus(status));
        }
        Page<ReservationResponse> page = reservationRepository.findAll(Specification.allOf(specs), pageable)
                .map(mapper::toResponse);
        return PageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> queueForBook(Long bookId) {
        return reservationRepository.findByBookIdOrderByCreatedAtAsc(bookId).stream()
                .filter(r -> ACTIVE_STATUSES.contains(r.getStatus()))
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public int expireStaleHolds() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        List<Reservation> stale = reservationRepository.findByStatusAndPickupExpiryBefore(ReservationStatus.READY, today);
        for (Reservation reservation : stale) {
            BookCopy heldCopy = reservation.getHeldCopy();
            reservation.setStatus(ReservationStatus.EXPIRED);
            reservation.setHeldCopy(null);
            reservationRepository.save(reservation);
            if (heldCopy != null) {
                releaseHold(heldCopy);
            }
        }
        return stale.size();
    }

    @Override
    @Transactional
    public boolean holdForNextInQueue(BookCopy copy) {
        Optional<Reservation> next = reservationRepository
                .findFirstByBookIdAndStatusOrderByCreatedAtAsc(copy.getBook().getId(), ReservationStatus.PENDING);
        if (next.isEmpty()) {
            return false;
        }
        Reservation reservation = next.get();
        int holdDays = settingsService.getLibrarySettings().getReservationHoldDays();
        reservation.setStatus(ReservationStatus.READY);
        reservation.setReadyAt(Instant.now());
        reservation.setPickupExpiry(LocalDate.now(ZoneOffset.UTC).plusDays(holdDays));
        reservation.setHeldCopy(copy);
        reservationRepository.save(reservation);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPendingReservation(Long bookId) {
        return reservationRepository.existsByBookIdAndStatus(bookId, ReservationStatus.PENDING);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Reservation> findActiveHold(Long copyId) {
        return reservationRepository.findByHeldCopyIdAndStatus(copyId, ReservationStatus.READY);
    }

    @Override
    @Transactional
    public void markFulfilled(Reservation reservation) {
        reservation.setStatus(ReservationStatus.FULFILLED);
        reservationRepository.save(reservation);
    }

    /**
     * Passes a freed copy to the next waiting reservation if any; otherwise makes it available.
     */
    private void releaseHold(BookCopy copy) {
        boolean rehandled = holdForNextInQueue(copy);
        copy.setStatus(rehandled ? BookCopyStatus.RESERVED : BookCopyStatus.AVAILABLE);
        bookCopyRepository.save(copy);
        bookCountService.refresh(copy.getBook().getId());
    }

    private Reservation getEntity(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("đặt trước", id));
    }
}
