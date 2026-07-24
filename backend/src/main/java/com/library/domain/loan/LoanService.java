package com.library.domain.loan;

import com.library.common.PageResponse;
import com.library.domain.loan.dto.CheckoutRequest;
import com.library.domain.loan.dto.LoanResponse;
import com.library.domain.loan.dto.ReturnByBarcodeRequest;
import com.library.domain.loan.dto.ReturnRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface LoanService {

    LoanResponse checkout(CheckoutRequest request);

    LoanResponse renew(Long loanId);

    LoanResponse returnLoan(Long loanId, ReturnRequest request);

    LoanResponse returnByBarcode(ReturnByBarcodeRequest request);

    LoanResponse getById(Long loanId);

    PageResponse<LoanResponse> list(String search, Long memberId, LoanStatus status,
                                    LocalDate from, LocalDate to, Pageable pageable);
}
