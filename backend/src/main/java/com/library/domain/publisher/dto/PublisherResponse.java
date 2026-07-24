package com.library.domain.publisher.dto;

import java.time.Instant;

public record PublisherResponse(
        Long id,
        String name,
        String address,
        String phone,
        String email,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}
