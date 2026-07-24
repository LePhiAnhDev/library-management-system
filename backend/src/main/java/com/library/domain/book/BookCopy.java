package com.library.domain.book;

import com.library.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * A single physical copy of a book. This is the unit that is actually borrowed.
 * The @Version column enables optimistic locking so two concurrent checkouts of the same copy
 * cannot both succeed (the losing transaction gets a conflict and retries).
 */
@Entity
@Table(name = "book_copies")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookCopy extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "barcode", nullable = false, unique = true, length = 64)
    private String barcode;

    @Column(name = "shelf_location", length = 100)
    private String shelfLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BookCopyStatus status;

    @Column(name = "acquired_date")
    private LocalDate acquiredDate;

    @Column(name = "condition_note", length = 1000)
    private String conditionNote;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
