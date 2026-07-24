package com.library.domain.book.dto;

/**
 * Lightweight author reference embedded in a book response.
 */
public record AuthorSummary(Long id, String fullName) {
}
