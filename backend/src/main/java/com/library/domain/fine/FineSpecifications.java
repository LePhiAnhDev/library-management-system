package com.library.domain.fine;

import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

public final class FineSpecifications {

    private FineSpecifications() {
    }

    public static Specification<Fine> hasMember(Long memberId) {
        return (root, criteria, cb) -> cb.equal(root.get("member").get("id"), memberId);
    }

    public static Specification<Fine> hasType(FineType type) {
        return (root, criteria, cb) -> cb.equal(root.get("type"), type);
    }

    public static Specification<Fine> hasStatus(FineStatus status) {
        return (root, criteria, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Fine> createdFrom(LocalDate from) {
        Instant start = from.atStartOfDay(ZoneOffset.UTC).toInstant();
        return (root, criteria, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), start);
    }

    public static Specification<Fine> createdTo(LocalDate to) {
        Instant end = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        return (root, criteria, cb) -> cb.lessThan(root.get("createdAt"), end);
    }
}
