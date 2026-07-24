package com.library.domain.loan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long>, JpaSpecificationExecutor<Loan> {

    Optional<Loan> findByCode(String code);

    long countByMemberIdAndStatus(Long memberId, LoanStatus status);

    Optional<Loan> findByBookCopyIdAndStatus(Long bookCopyId, LoanStatus status);

    boolean existsByBookCopyId(Long bookCopyId);

    boolean existsByMemberId(Long memberId);

    @Query(value = "SELECT nextval('loan_code_seq')", nativeQuery = true)
    long nextLoanCodeSequence();
}
