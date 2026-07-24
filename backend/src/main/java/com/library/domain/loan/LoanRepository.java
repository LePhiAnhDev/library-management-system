package com.library.domain.loan;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long>, JpaSpecificationExecutor<Loan> {

    Optional<Loan> findByCode(String code);

    long countByMemberIdAndStatus(Long memberId, LoanStatus status);

    long countByStatus(LoanStatus status);

    Optional<Loan> findByBookCopyIdAndStatus(Long bookCopyId, LoanStatus status);

    boolean existsByBookCopyId(Long bookCopyId);

    boolean existsByMemberId(Long memberId);

    @Query(value = "SELECT nextval('loan_code_seq')", nativeQuery = true)
    long nextLoanCodeSequence();

    // Reporting aggregations.

    @Query("select count(l) from Loan l where l.status = com.library.domain.loan.LoanStatus.BORROWED and l.dueDate < :today")
    long countOverdue(@Param("today") LocalDate today);

    @Query("""
            select l.bookCopy.book.id, l.bookCopy.book.title, count(l)
            from Loan l
            where l.borrowDate between :from and :to
            group by l.bookCopy.book.id, l.bookCopy.book.title
            order by count(l) desc
            """)
    List<Object[]> topBorrowedBooks(@Param("from") LocalDate from, @Param("to") LocalDate to, Pageable pageable);

    @Query("""
            select l.member.id, l.member.memberCode, l.member.fullName, count(l)
            from Loan l
            where l.borrowDate between :from and :to
            group by l.member.id, l.member.memberCode, l.member.fullName
            order by count(l) desc
            """)
    List<Object[]> mostActiveMembers(@Param("from") LocalDate from, @Param("to") LocalDate to, Pageable pageable);

    @Query("""
            select l.borrowDate, count(l)
            from Loan l
            where l.borrowDate between :from and :to
            group by l.borrowDate
            order by l.borrowDate
            """)
    List<Object[]> loansPerDay(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
