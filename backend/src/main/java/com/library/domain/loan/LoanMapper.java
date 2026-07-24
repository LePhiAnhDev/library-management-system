package com.library.domain.loan;

import com.library.domain.loan.dto.LoanResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface LoanMapper {

    @Mapping(target = "memberId", source = "member.id")
    @Mapping(target = "memberCode", source = "member.memberCode")
    @Mapping(target = "memberName", source = "member.fullName")
    @Mapping(target = "bookCopyId", source = "bookCopy.id")
    @Mapping(target = "barcode", source = "bookCopy.barcode")
    @Mapping(target = "bookId", source = "bookCopy.book.id")
    @Mapping(target = "bookTitle", source = "bookCopy.book.title")
    @Mapping(target = "status", expression = "java(deriveStatus(loan))")
    @Mapping(target = "overdue", expression = "java(isOverdue(loan))")
    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "createdByName", source = "createdBy.fullName")
    @Mapping(target = "returnedById", source = "returnedBy.id")
    @Mapping(target = "returnedByName", source = "returnedBy.fullName")
    LoanResponse toResponse(Loan loan);

    /**
     * OVERDUE is derived, never stored, using the single predicate status=BORROWED AND dueDate < today.
     */
    default String deriveStatus(Loan loan) {
        return isOverdue(loan) ? LoanStatus.OVERDUE.name() : loan.getStatus().name();
    }

    default boolean isOverdue(Loan loan) {
        return loan.getStatus() == LoanStatus.BORROWED
                && loan.getDueDate() != null
                && loan.getDueDate().isBefore(LocalDate.now(ZoneOffset.UTC));
    }
}
