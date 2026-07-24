package com.library.domain.book;

import com.library.common.RecordStatus;
import org.springframework.data.jpa.domain.Specification;

/**
 * Dynamic filters for the book catalog (Specification pattern).
 */
public final class BookSpecifications {

    private BookSpecifications() {
    }

    public static Specification<Book> keyword(String query) {
        String pattern = "%" + query.trim().toLowerCase() + "%";
        return (root, criteria, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), pattern),
                cb.like(cb.lower(root.get("isbn")), pattern),
                cb.like(cb.lower(root.get("subtitle")), pattern));
    }

    public static Specification<Book> hasCategory(Long categoryId) {
        return (root, criteria, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Book> hasPublisher(Long publisherId) {
        return (root, criteria, cb) -> cb.equal(root.get("publisher").get("id"), publisherId);
    }

    public static Specification<Book> hasAuthor(Long authorId) {
        return (root, criteria, cb) -> {
            criteria.distinct(true);
            return cb.equal(root.join("authors").get("id"), authorId);
        };
    }

    public static Specification<Book> hasStatus(RecordStatus status) {
        return (root, criteria, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Book> availableOnly() {
        return (root, criteria, cb) -> cb.greaterThan(root.get("availableCopies"), 0);
    }
}
