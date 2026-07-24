package com.library.domain.author.dto;

import java.time.Instant;

public record AuthorResponse(
        Long id,
        String fullName,
        String biography,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}
