package com.library.security.clerk;

/**
 * Subset of a Clerk user record needed to populate the internal staff profile.
 */
public record ClerkUserInfo(String email, String fullName, String avatarUrl) {
}
