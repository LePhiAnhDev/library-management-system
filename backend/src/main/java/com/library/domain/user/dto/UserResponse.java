package com.library.domain.user.dto;

import java.time.Instant;

/**
 * Internal profile of the currently authenticated staff member.
 */
public record UserResponse(
        Long id,
        String clerkUserId,
        String email,
        String fullName,
        String avatarUrl,
        String status,
        Instant createdAt
) {
}
