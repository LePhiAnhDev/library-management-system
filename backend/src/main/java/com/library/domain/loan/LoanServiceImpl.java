package com.library.domain.loan;

import com.library.common.PageResponse;
import com.library.domain.book.BookCopy;
import com.library.domain.book.BookCopyRepository;
import com.library.domain.book.BookCopyStatus;
import com.library.domain.book.BookCountService;
import com.library.domain.fine.FineService;
import com.library.domain.loan.dto.CheckoutRequest;
import com.library.domain.loan.dto.LoanResponse;
import com.library.domain.loan.dto.ReturnByBarcodeRequest;
import com.library.domain.loan.dto.ReturnRequest;
import com.library.domain.member.Member;
import com.library.domain.member.MemberRepository;
import com.library.domain.member.MemberStatus;
import com.library.domain.reservation.Reservation;
import com.library.domain.reservation.ReservationService;
import com.library.domain.settings.LibrarySettings;
import com.library.domain.settings.LoanPolicy;
import com.library.domain.settings.SettingsService;
import com.library.exception.BusinessRuleException;
import com.library.exception.ConflictException;
import com.library.exception.ResourceNotFoundException;
import com.library.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final LoanMapper loanMapper;
    private final MemberRepository memberRepository;
    private final BookCopyRepository bookCopyRepository;
    private final BookCountService bookCountService;
    private final SettingsService settingsService;
    private final FineService fineService;
    private final CurrentUserService currentUserService;
    private final ReservationService reservationService;

    @Override
    @Transactional
    public LoanResponse checkout(CheckoutRequest request) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> ResourceNotFoundException.of("độc giả", request.memberId()));

        validateMemberEligibility(member, today);

        BookCopy copy = resolveCopy(request);
        Reservation heldReservation = resolveReservedCopyHold(copy, member);
        Long bookId = copy.getBook().getId();

        LoanPolicy policy = settingsService.getLoanPolicy(member.getMembershipType());
        // Flip the copy to BORROWED. The @Version on BookCopy makes a concurrent checkout of the
        // same copy fail with an optimistic lock conflict (mapped to 409) so it can never double borrow.
        copy.setStatus(BookCopyStatus.BORROWED);
        bookCopyRepository.save(copy);

        Loan loan = Loan.builder()
                .code(generateLoanCode())
                .member(member)
                .bookCopy(copy)
                .borrowDate(today)
                .dueDate(today.plusDays(policy.getLoanPeriodDays()))
                .status(LoanStatus.BORROWED)
                .renewCount(0)
                .createdBy(currentUserService.getCurrentUser())
                .build();
        Loan saved = loanRepository.save(loan);

        if (heldReservation != null) {
            reservationService.markFulfilled(heldReservation);
        }
        bookCountService.refresh(bookId);
        return loanMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public LoanResponse renew(Long loanId) {
        Loan loan = getEntity(loanId);
        if (loan.getStatus() != LoanStatus.BORROWED) {
            throw new BusinessRuleException("Chỉ có thể gia hạn phiếu đang mượn");
        }
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        if (loan.getDueDate().isBefore(today)) {
            throw new BusinessRuleException("Không thể gia hạn phiếu đã quá hạn");
        }
        LoanPolicy policy = settingsService.getLoanPolicy(loan.getMember().getMembershipType());
        if (loan.getRenewCount() >= policy.getMaxRenewals()) {
            throw new BusinessRuleException("Đã đạt số lần gia hạn tối đa");
        }
        if (reservationService.hasPendingReservation(loan.getBookCopy().getBook().getId())) {
            throw new BusinessRuleException("Không thể gia hạn vì sách đang có người đặt trước");
        }
        loan.setDueDate(loan.getDueDate().plusDays(policy.getLoanPeriodDays()));
        loan.setRenewCount(loan.getRenewCount() + 1);
        return loanMapper.toResponse(loanRepository.save(loan));
    }

    @Override
    @Transactional
    public LoanResponse returnLoan(Long loanId, ReturnRequest request) {
        return doReturn(getEntity(loanId), request.condition(), request.note(), request.overrideFee());
    }

    @Override
    @Transactional
    public LoanResponse returnByBarcode(ReturnByBarcodeRequest request) {
        BookCopy copy = bookCopyRepository.findByBarcode(request.barcode().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản sao với mã vạch " + request.barcode()));
        Loan loan = loanRepository.findByBookCopyIdAndStatus(copy.getId(), LoanStatus.BORROWED)
                .orElseThrow(() -> new BusinessRuleException("Không có phiếu mượn đang hoạt động cho bản sao này"));
        return doReturn(loan, request.condition(), request.note(), request.overrideFee());
    }

    @Override
    @Transactional(readOnly = true)
    public LoanResponse getById(Long loanId) {
        return loanMapper.toResponse(getEntity(loanId));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LoanResponse> list(String search, Long memberId, LoanStatus status,
                                           LocalDate from, LocalDate to, Pageable pageable) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        List<Specification<Loan>> specs = new ArrayList<>();
        if (StringUtils.hasText(search)) {
            specs.add(LoanSpecifications.codeContains(search));
        }
        if (memberId != null) {
            specs.add(LoanSpecifications.hasMember(memberId));
        }
        if (status != null) {
            specs.add(LoanSpecifications.effectiveStatus(status, today));
        }
        if (from != null) {
            specs.add(LoanSpecifications.borrowedFrom(from));
        }
        if (to != null) {
            specs.add(LoanSpecifications.borrowedTo(to));
        }
        Page<LoanResponse> page = loanRepository.findAll(Specification.allOf(specs), pageable).map(loanMapper::toResponse);
        return PageResponse.from(page);
    }

    private LoanResponse doReturn(Loan loan, CopyReturnCondition requestedCondition, String note, BigDecimal overrideFee) {
        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new BusinessRuleException("Phiếu mượn đã được trả");
        }
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        CopyReturnCondition condition = requestedCondition != null ? requestedCondition : CopyReturnCondition.NORMAL;
        BookCopy copy = loan.getBookCopy();
        Long bookId = copy.getBook().getId();

        loan.setReturnDate(today);
        loan.setStatus(LoanStatus.RETURNED);
        loan.setReturnedBy(currentUserService.getCurrentUser());

        switch (condition) {
            case LOST -> {
                copy.setStatus(BookCopyStatus.LOST);
                applyNote(copy, note);
                fineService.createLostFine(loan.getMember(), loan, overrideFee);
            }
            case DAMAGED -> {
                copy.setStatus(BookCopyStatus.DAMAGED);
                applyNote(copy, note);
                fineService.createDamagedFine(loan.getMember(), loan, overrideFee);
            }
            case NORMAL -> {
                // Hand the freed copy to the next reservation in the queue, else make it available.
                boolean held = reservationService.holdForNextInQueue(copy);
                copy.setStatus(held ? BookCopyStatus.RESERVED : BookCopyStatus.AVAILABLE);
            }
        }
        bookCopyRepository.save(copy);

        // Overdue fine applies when the copy physically came back late. A lost copy is charged the
        // replacement fee only (it will not be returned late in the usual sense).
        if (condition != CopyReturnCondition.LOST && loan.getDueDate().isBefore(today)) {
            long daysLate = ChronoUnit.DAYS.between(loan.getDueDate(), today);
            fineService.createOverdueFine(loan.getMember(), loan, daysLate);
        }

        loanRepository.save(loan);
        bookCountService.refresh(bookId);
        return loanMapper.toResponse(loan);
    }

    private void validateMemberEligibility(Member member, LocalDate today) {
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessRuleException("Độc giả không ở trạng thái hoạt động");
        }
        if (member.getExpiryDate() != null && member.getExpiryDate().isBefore(today)) {
            throw new BusinessRuleException("Thẻ độc giả đã hết hạn");
        }
        LoanPolicy policy = settingsService.getLoanPolicy(member.getMembershipType());
        long activeLoans = loanRepository.countByMemberIdAndStatus(member.getId(), LoanStatus.BORROWED);
        if (activeLoans >= policy.getMaxBooks()) {
            throw new BusinessRuleException("Độc giả đã mượn tối đa " + policy.getMaxBooks() + " cuốn");
        }
        BigDecimal unpaid = fineService.totalUnpaid(member.getId());
        LibrarySettings settings = settingsService.getLibrarySettings();
        if (unpaid.compareTo(settings.getFineBlockThreshold()) > 0) {
            throw new BusinessRuleException("Độc giả còn phạt chưa thanh toán vượt ngưỡng cho phép");
        }
    }

    /**
     * When borrowing a RESERVED copy, it must be the hold belonging to this member (reservation pickup).
     * AVAILABLE copies borrow normally; any other status is not lendable.
     */
    private Reservation resolveReservedCopyHold(BookCopy copy, Member member) {
        if (copy.getStatus() == BookCopyStatus.AVAILABLE) {
            return null;
        }
        if (copy.getStatus() == BookCopyStatus.RESERVED) {
            Reservation hold = reservationService.findActiveHold(copy.getId())
                    .orElseThrow(() -> new ConflictException("Bản sao đang được giữ chỗ nhưng không xác định được đặt trước"));
            if (!hold.getMember().getId().equals(member.getId())) {
                throw new ConflictException("Bản sao đang được giữ cho độc giả khác");
            }
            return hold;
        }
        throw new ConflictException("Bản sao không sẵn sàng để cho mượn");
    }

    private BookCopy resolveCopy(CheckoutRequest request) {
        if (request.bookCopyId() != null) {
            return bookCopyRepository.findById(request.bookCopyId())
                    .orElseThrow(() -> ResourceNotFoundException.of("bản sao", request.bookCopyId()));
        }
        if (StringUtils.hasText(request.barcode())) {
            return bookCopyRepository.findByBarcode(request.barcode().trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản sao với mã vạch " + request.barcode()));
        }
        throw new BusinessRuleException("Cần chọn bản sao hoặc quét mã vạch");
    }

    private void applyNote(BookCopy copy, String note) {
        if (StringUtils.hasText(note)) {
            copy.setConditionNote(note);
        }
    }

    private Loan getEntity(Long loanId) {
        return loanRepository.findById(loanId)
                .orElseThrow(() -> ResourceNotFoundException.of("phiếu mượn", loanId));
    }

    private String generateLoanCode() {
        return "LN" + String.format("%06d", loanRepository.nextLoanCodeSequence());
    }
}
