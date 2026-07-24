package com.library.domain.book;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(String isbn, Long id);

    boolean existsByCategoryId(Long categoryId);

    boolean existsByPublisherId(Long publisherId);

    boolean existsByAuthorsId(Long authorId);

    @EntityGraph(attributePaths = {"authors", "category", "publisher"})
    Optional<Book> findWithDetailsById(Long id);
}
