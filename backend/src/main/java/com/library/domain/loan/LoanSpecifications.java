package com.library.domain.loan;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class LoanSpecifications {

    private LoanSpecifications() {
    }

    public static Specification<Loan> codeContains(String query) {
        String pattern = "%" + query.trim().toLowerCase() + "%";
        return (root, criteria, cb) -> cb.like(cb.lower(root.get("code")), pattern);
    }

    public static Specification<Loan> hasMember(Long memberId) {
        return (root, criteria, cb) -> cb.equal(root.get("member").get("id"), memberId);
    }

    /**
     * Filter by the effective (derived) status. OVERDUE and BORROWED both map to the stored
     * BORROWED status split by the due date, using the same predicate as the response mapping.
     */
    public static Specification<Loan> effectiveStatus(LoanStatus status, LocalDate today) {
        return switch (status) {
            case RETURNED -> (root, criteria, cb) -> cb.equal(root.get("status"), LoanStatus.RETURNED);
            case BORROWED -> (root, criteria, cb) -> cb.and(
                    cb.equal(root.get("status"), LoanStatus.BORROWED),
                    cb.greaterThanOrEqualTo(root.get("dueDate"), today));
            case OVERDUE -> (root, criteria, cb) -> cb.and(
                    cb.equal(root.get("status"), LoanStatus.BORROWED),
                    cb.lessThan(root.get("dueDate"), today));
        };
    }

    public static Specification<Loan> borrowedFrom(LocalDate from) {
        return (root, criteria, cb) -> cb.greaterThanOrEqualTo(root.get("borrowDate"), from);
    }

    public static Specification<Loan> borrowedTo(LocalDate to) {
        return (root, criteria, cb) -> cb.lessThanOrEqualTo(root.get("borrowDate"), to);
    }

    public static Specification<Loan> overdue(LocalDate today) {
        return (root, criteria, cb) -> cb.and(
                cb.equal(root.get("status"), LoanStatus.BORROWED),
                cb.lessThan(root.get("dueDate"), today));
    }
}
