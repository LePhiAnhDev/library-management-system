package com.library.domain.user;

/**
 * Lifecycle of an internal staff account provisioned from Clerk.
 * There are no roles; status only reflects whether the account is active.
 */
public enum UserStatus {
    ACTIVE,
    INACTIVE
}
