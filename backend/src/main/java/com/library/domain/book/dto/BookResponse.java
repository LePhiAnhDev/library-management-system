package com.library.domain.book.dto;

import java.time.Instant;
import java.util.List;

public record BookResponse(
        Long id,
        String isbn,
        String title,
        String subtitle,
        String description,
        Long publisherId,
        String publisherName,
        Long categoryId,
        String categoryName,
        List<AuthorSummary> authors,
        Integer publicationYear,
        String language,
        Integer pageCount,
        String coverImageUrl,
        Integer totalCopies,
        Integer availableCopies,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}
