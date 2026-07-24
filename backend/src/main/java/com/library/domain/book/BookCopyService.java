package com.library.domain.book;

import com.library.domain.book.dto.BookCopyRequest;
import com.library.domain.book.dto.BookCopyResponse;
import com.library.domain.book.dto.BookCopyStatusRequest;

import java.util.List;

public interface BookCopyService {

    BookCopyResponse addCopy(Long bookId, BookCopyRequest request);

    BookCopyResponse updateCopy(Long copyId, BookCopyRequest request);

    BookCopyResponse changeStatus(Long copyId, BookCopyStatusRequest request);

    BookCopyResponse getById(Long copyId);

    List<BookCopyResponse> listByBook(Long bookId);

    void deleteCopy(Long copyId);
}
