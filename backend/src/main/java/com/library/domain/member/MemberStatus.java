package com.library.domain.member;

/**
 * Reader account state. Only ACTIVE members within their card validity may borrow.
 */
public enum MemberStatus {
    ACTIVE,
    SUSPENDED,
    EXPIRED
}
