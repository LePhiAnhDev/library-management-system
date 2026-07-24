package com.library.domain.book;

import com.library.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Recomputes the denormalized copy counters on a book from the actual copy rows.
 * Called inside the same transaction as any copy status change (add, remove, borrow, return),
 * so total_copies and available_copies always reflect reality.
 */
@Service
@RequiredArgsConstructor
public class BookCountService {

    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;

    @Transactional
    public void refresh(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> ResourceNotFoundException.of("sách", bookId));
        long total = bookCopyRepository.countByBookId(bookId);
        long available = bookCopyRepository.countByBookIdAndStatus(bookId, BookCopyStatus.AVAILABLE);
        book.setTotalCopies((int) total);
        book.setAvailableCopies((int) available);
        bookRepository.save(book);
    }
}
