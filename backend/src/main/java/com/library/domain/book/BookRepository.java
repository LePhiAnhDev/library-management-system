package com.library.domain.book;

import com.library.common.RecordStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    /**
     * Acquires a write lock on the book row so availability recounts (add/remove/borrow/return copies)
     * are serialized per book. Prevents lost updates on the denormalized available_copies counter.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Book b where b.id = :id")
    Optional<Book> lockById(@Param("id") Long id);

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(String isbn, Long id);

    boolean existsByCategoryId(Long categoryId);

    boolean existsByPublisherId(Long publisherId);

    boolean existsByAuthorsId(Long authorId);

    long countByStatus(RecordStatus status);

    @EntityGraph(attributePaths = {"authors", "category", "publisher"})
    Optional<Book> findWithDetailsById(Long id);
}
