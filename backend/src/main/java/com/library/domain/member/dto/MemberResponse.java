package com.library.domain.member.dto;

import java.time.Instant;
import java.time.LocalDate;

public record MemberResponse(
        Long id,
        String memberCode,
        String fullName,
        String email,
        String phone,
        String address,
        String membershipType,
        LocalDate joinDate,
        LocalDate expiryDate,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}
