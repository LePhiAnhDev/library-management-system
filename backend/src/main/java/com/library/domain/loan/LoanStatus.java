package com.library.domain.loan;

/**
 * Loan state. Only BORROWED and RETURNED are ever persisted; OVERDUE is derived on read
 * with the predicate (status = BORROWED AND due_date < today) so all paths agree.
 */
public enum LoanStatus {
    BORROWED,
    RETURNED,
    OVERDUE
}
