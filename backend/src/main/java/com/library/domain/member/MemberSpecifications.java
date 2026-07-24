package com.library.domain.member;

import org.springframework.data.jpa.domain.Specification;

public final class MemberSpecifications {

    private MemberSpecifications() {
    }

    public static Specification<Member> keyword(String query) {
        String pattern = "%" + query.trim().toLowerCase() + "%";
        return (root, criteria, cb) -> cb.or(
                cb.like(cb.lower(root.get("fullName")), pattern),
                cb.like(cb.lower(root.get("memberCode")), pattern),
                cb.like(cb.lower(root.get("email")), pattern),
                cb.like(cb.lower(root.get("phone")), pattern));
    }

    public static Specification<Member> hasStatus(MemberStatus status) {
        return (root, criteria, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Member> hasMembershipType(MembershipType type) {
        return (root, criteria, cb) -> cb.equal(root.get("membershipType"), type);
    }
}
