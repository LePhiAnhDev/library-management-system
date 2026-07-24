package com.library.domain.settings;

import com.library.domain.member.MembershipType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Borrowing rules per membership type. Editable via settings; seeded with sensible defaults.
 */
@Entity
@Table(name = "loan_policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanPolicy {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "membership_type", length = 20)
    private MembershipType membershipType;

    @Column(name = "max_books", nullable = false)
    private int maxBooks;

    @Column(name = "loan_period_days", nullable = false)
    private int loanPeriodDays;

    @Column(name = "max_renewals", nullable = false)
    private int maxRenewals;
}
