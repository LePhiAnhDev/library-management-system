package com.library.domain.category.dto;

import java.time.Instant;

public record CategoryResponse(
        Long id,
        String name,
        String description,
        Long parentId,
        String parentName,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}
