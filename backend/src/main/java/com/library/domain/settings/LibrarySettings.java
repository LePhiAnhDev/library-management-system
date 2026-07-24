package com.library.domain.settings;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Single-row (id = 1) library-wide configuration. Money values are VND (whole numbers) as BigDecimal.
 */
@Entity
@Table(name = "library_settings")
@Getter
@Setter
public class LibrarySettings {

    public static final Long SINGLETON_ID = 1L;

    @Id
    private Long id;

    @Column(name = "library_name", nullable = false)
    private String libraryName;

    @Column(name = "library_address", length = 500)
    private String libraryAddress;

    @Column(name = "overdue_fine_per_day", nullable = false, precision = 15, scale = 2)
    private BigDecimal overdueFinePerDay;

    @Column(name = "fine_block_threshold", nullable = false, precision = 15, scale = 2)
    private BigDecimal fineBlockThreshold;

    @Column(name = "reservation_hold_days", nullable = false)
    private int reservationHoldDays;

    @Column(name = "lost_default_fee", nullable = false, precision = 15, scale = 2)
    private BigDecimal lostDefaultFee;

    @Column(name = "damaged_default_fee", nullable = false, precision = 15, scale = 2)
    private BigDecimal damagedDefaultFee;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
