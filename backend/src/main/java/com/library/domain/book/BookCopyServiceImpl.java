package com.library.domain.book;

import com.library.domain.book.dto.BookCopyRequest;
import com.library.domain.book.dto.BookCopyResponse;
import com.library.domain.book.dto.BookCopyStatusRequest;
import com.library.domain.loan.LoanRepository;
import com.library.exception.BusinessRuleException;
import com.library.exception.DuplicateResourceException;
import com.library.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookCopyServiceImpl implements BookCopyService {

    private static final Set<BookCopyStatus> MANUAL_STATUSES = Set.of(
            BookCopyStatus.AVAILABLE, BookCopyStatus.LOST, BookCopyStatus.DAMAGED, BookCopyStatus.MAINTENANCE);

    private final BookCopyRepository bookCopyRepository;
    private final BookRepository bookRepository;
    private final BookCopyMapper mapper;
    private final BookCountService countService;
    private final LoanRepository loanRepository;

    @Override
    @Transactional
    public BookCopyResponse addCopy(Long bookId, BookCopyRequest request) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> ResourceNotFoundException.of("sách", bookId));
        if (bookCopyRepository.existsByBarcode(request.barcode())) {
            throw new DuplicateResourceException("Mã vạch đã tồn tại: " + request.barcode());
        }
        BookCopy copy = new BookCopy();
        mapper.updateEntity(request, copy);
        copy.setBook(book);
        copy.setStatus(BookCopyStatus.AVAILABLE);
        BookCopy saved = bookCopyRepository.save(copy);
        countService.refresh(bookId);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BookCopyResponse updateCopy(Long copyId, BookCopyRequest request) {
        BookCopy copy = getEntity(copyId);
        if (bookCopyRepository.existsByBarcodeAndIdNot(request.barcode(), copyId)) {
            throw new DuplicateResourceException("Mã vạch đã tồn tại: " + request.barcode());
        }
        mapper.updateEntity(request, copy);
        return mapper.toResponse(bookCopyRepository.save(copy));
    }

    @Override
    @Transactional
    public BookCopyResponse changeStatus(Long copyId, BookCopyStatusRequest request) {
        BookCopy copy = getEntity(copyId);
        if (!MANUAL_STATUSES.contains(request.status())) {
            throw new BusinessRuleException("Trạng thái này chỉ thay đổi qua nghiệp vụ mượn hoặc giữ chỗ");
        }
        if (copy.getStatus() == BookCopyStatus.BORROWED || copy.getStatus() == BookCopyStatus.RESERVED) {
            throw new BusinessRuleException("Bản sao đang được mượn hoặc giữ chỗ, cần xử lý trả trước khi đổi trạng thái");
        }
        copy.setStatus(request.status());
        if (request.conditionNote() != null) {
            copy.setConditionNote(request.conditionNote());
        }
        bookCopyRepository.save(copy);
        countService.refresh(copy.getBook().getId());
        return mapper.toResponse(copy);
    }

    @Override
    @Transactional(readOnly = true)
    public BookCopyResponse getById(Long copyId) {
        return mapper.toResponse(getEntity(copyId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookCopyResponse> listByBook(Long bookId) {
        return bookCopyRepository.findByBookIdOrderByBarcodeAsc(bookId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteCopy(Long copyId) {
        BookCopy copy = getEntity(copyId);
        if (copy.getStatus() == BookCopyStatus.BORROWED || copy.getStatus() == BookCopyStatus.RESERVED) {
            throw new BusinessRuleException("Không thể xóa bản sao đang được mượn hoặc đang giữ chỗ");
        }
        if (loanRepository.existsByBookCopyId(copyId)) {
            throw new BusinessRuleException("Không thể xóa bản sao đã có lịch sử mượn");
        }
        Long bookId = copy.getBook().getId();
        bookCopyRepository.delete(copy);
        countService.refresh(bookId);
    }

    private BookCopy getEntity(Long copyId) {
        return bookCopyRepository.findById(copyId)
                .orElseThrow(() -> ResourceNotFoundException.of("bản sao", copyId));
    }
}
