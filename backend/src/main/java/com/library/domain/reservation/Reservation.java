package com.library.domain.reservation;

import com.library.common.BaseEntity;
import com.library.domain.book.Book;
import com.library.domain.book.BookCopy;
import com.library.domain.member.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.time.Instant;
import java.time.LocalDate;

/**
 * A hold on a book (title level). The queue is ordered by createdAt (which is the reservation date).
 * When a copy is returned and this is the earliest PENDING reservation, it becomes READY with a
 * specific held copy and a pickup deadline.
 */
@Entity
@Table(name = "reservations")
@BatchSize(size = 64)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "held_copy_id")
    private BookCopy heldCopy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReservationStatus status;

    @Column(name = "ready_at")
    private Instant readyAt;

    @Column(name = "pickup_expiry")
    private LocalDate pickupExpiry;
}
