package com.library.domain.book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BookCopyRepository extends JpaRepository<BookCopy, Long>, JpaSpecificationExecutor<BookCopy> {

    long countByStatus(BookCopyStatus status);

    @Query("select c.status, count(c) from BookCopy c group by c.status")
    List<Object[]> inventoryByStatus();

    boolean existsByBarcode(String barcode);

    boolean existsByBarcodeAndIdNot(String barcode, Long id);

    Optional<BookCopy> findByBarcode(String barcode);

    long countByBookId(Long bookId);

    long countByBookIdAndStatus(Long bookId, BookCopyStatus status);

    List<BookCopy> findByBookIdOrderByBarcodeAsc(Long bookId);
}
