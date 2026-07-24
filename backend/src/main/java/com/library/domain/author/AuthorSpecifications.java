package com.library.domain.author;

import com.library.common.RecordStatus;
import org.springframework.data.jpa.domain.Specification;

public final class AuthorSpecifications {

    private AuthorSpecifications() {
    }

    public static Specification<Author> nameContains(String query) {
        String pattern = "%" + query.trim().toLowerCase() + "%";
        return (root, criteria, cb) -> cb.like(cb.lower(root.get("fullName")), pattern);
    }

    public static Specification<Author> hasStatus(RecordStatus status) {
        return (root, criteria, cb) -> cb.equal(root.get("status"), status);
    }
}
