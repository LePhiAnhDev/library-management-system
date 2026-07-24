package com.library.domain.book;

import com.library.common.PageResponse;
import com.library.common.RecordStatus;
import com.library.domain.book.dto.BookRequest;
import com.library.domain.book.dto.BookResponse;
import org.springframework.data.domain.Pageable;

public interface BookService {

    BookResponse create(BookRequest request);

    BookResponse update(Long id, BookRequest request);

    BookResponse getById(Long id);

    PageResponse<BookResponse> list(String search, Long categoryId, Long publisherId, Long authorId,
                                    Boolean availableOnly, RecordStatus status, Pageable pageable);

    BookResponse updateCoverImage(Long id, String coverImageUrl);

    void delete(Long id);
}
