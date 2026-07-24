package com.library.domain.book;

import com.library.common.PageResponse;
import com.library.common.RecordStatus;
import com.library.domain.author.Author;
import com.library.domain.author.AuthorRepository;
import com.library.domain.book.dto.BookRequest;
import com.library.domain.book.dto.BookResponse;
import com.library.domain.category.Category;
import com.library.domain.category.CategoryRepository;
import com.library.domain.publisher.Publisher;
import com.library.domain.publisher.PublisherRepository;
import com.library.exception.BusinessRuleException;
import com.library.exception.DuplicateResourceException;
import com.library.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final BookMapper mapper;
    private final CategoryRepository categoryRepository;
    private final PublisherRepository publisherRepository;
    private final AuthorRepository authorRepository;

    @Override
    @Transactional
    public BookResponse create(BookRequest request) {
        if (bookRepository.existsByIsbn(request.isbn())) {
            throw new DuplicateResourceException("ISBN đã tồn tại: " + request.isbn());
        }
        Book book = new Book();
        mapper.updateEntity(request, book);
        book.setCategory(resolveCategory(request.categoryId()));
        book.setPublisher(resolvePublisher(request.publisherId()));
        book.setAuthors(resolveAuthors(request.authorIds()));
        book.setTotalCopies(0);
        book.setAvailableCopies(0);
        book.setStatus(RecordStatus.ACTIVE);
        return mapper.toResponse(bookRepository.save(book));
    }

    @Override
    @Transactional
    public BookResponse update(Long id, BookRequest request) {
        Book book = getDetailedEntity(id);
        if (bookRepository.existsByIsbnAndIdNot(request.isbn(), id)) {
            throw new DuplicateResourceException("ISBN đã tồn tại: " + request.isbn());
        }
        mapper.updateEntity(request, book);
        book.setCategory(resolveCategory(request.categoryId()));
        book.setPublisher(resolvePublisher(request.publisherId()));
        book.getAuthors().clear();
        book.getAuthors().addAll(resolveAuthors(request.authorIds()));
        return mapper.toResponse(bookRepository.save(book));
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponse getById(Long id) {
        return mapper.toResponse(getDetailedEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BookResponse> list(String search, Long categoryId, Long publisherId, Long authorId,
                                           Boolean availableOnly, RecordStatus status, Pageable pageable) {
        List<Specification<Book>> specs = new ArrayList<>();
        if (StringUtils.hasText(search)) {
            specs.add(BookSpecifications.keyword(search));
        }
        if (categoryId != null) {
            specs.add(BookSpecifications.hasCategory(categoryId));
        }
        if (publisherId != null) {
            specs.add(BookSpecifications.hasPublisher(publisherId));
        }
        if (authorId != null) {
            specs.add(BookSpecifications.hasAuthor(authorId));
        }
        if (Boolean.TRUE.equals(availableOnly)) {
            specs.add(BookSpecifications.availableOnly());
        }
        if (status != null) {
            specs.add(BookSpecifications.hasStatus(status));
        }
        Page<BookResponse> page = bookRepository.findAll(Specification.allOf(specs), pageable).map(mapper::toResponse);
        return PageResponse.from(page);
    }

    @Override
    @Transactional
    public BookResponse updateCoverImage(Long id, String coverImageUrl) {
        Book book = getDetailedEntity(id);
        book.setCoverImageUrl(coverImageUrl);
        return mapper.toResponse(bookRepository.save(book));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("sách", id));
        boolean inCirculation = bookCopyRepository.countByBookIdAndStatus(id, BookCopyStatus.BORROWED) > 0
                || bookCopyRepository.countByBookIdAndStatus(id, BookCopyStatus.RESERVED) > 0;
        if (inCirculation) {
            throw new BusinessRuleException("Không thể xóa sách đang có bản sao được mượn hoặc đang giữ chỗ");
        }
        book.setStatus(RecordStatus.INACTIVE);
        bookRepository.save(book);
    }

    private Book getDetailedEntity(Long id) {
        return bookRepository.findWithDetailsById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("sách", id));
    }

    private Category resolveCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thể loại với id " + categoryId));
    }

    private Publisher resolvePublisher(Long publisherId) {
        if (publisherId == null) {
            return null;
        }
        return publisherRepository.findById(publisherId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà xuất bản với id " + publisherId));
    }

    private Set<Author> resolveAuthors(Set<Long> authorIds) {
        if (authorIds == null || authorIds.isEmpty()) {
            return new LinkedHashSet<>();
        }
        List<Author> found = authorRepository.findAllById(authorIds);
        if (found.size() != authorIds.size()) {
            throw new ResourceNotFoundException("Một hoặc nhiều tác giả không tồn tại");
        }
        return new LinkedHashSet<>(found);
    }
}
