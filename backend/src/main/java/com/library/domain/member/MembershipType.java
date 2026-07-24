package com.library.domain.member;

/**
 * Membership tier. Each type maps to a loan policy (max books, loan period, renewals)
 * resolved from system settings at borrow time.
 */
public enum MembershipType {
    REGULAR,
    STUDENT,
    PREMIUM
}
