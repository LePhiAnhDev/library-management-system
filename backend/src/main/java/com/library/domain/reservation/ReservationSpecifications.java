package com.library.domain.reservation;

import org.springframework.data.jpa.domain.Specification;

public final class ReservationSpecifications {

    private ReservationSpecifications() {
    }

    public static Specification<Reservation> hasBook(Long bookId) {
        return (root, criteria, cb) -> cb.equal(root.get("book").get("id"), bookId);
    }

    public static Specification<Reservation> hasMember(Long memberId) {
        return (root, criteria, cb) -> cb.equal(root.get("member").get("id"), memberId);
    }

    public static Specification<Reservation> hasStatus(ReservationStatus status) {
        return (root, criteria, cb) -> cb.equal(root.get("status"), status);
    }
}
