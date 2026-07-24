package com.library.domain.publisher;

import com.library.common.RecordStatus;
import org.springframework.data.jpa.domain.Specification;

public final class PublisherSpecifications {

    private PublisherSpecifications() {
    }

    public static Specification<Publisher> nameContains(String query) {
        String pattern = "%" + query.trim().toLowerCase() + "%";
        return (root, criteria, cb) -> cb.like(cb.lower(root.get("name")), pattern);
    }

    public static Specification<Publisher> hasStatus(RecordStatus status) {
        return (root, criteria, cb) -> cb.equal(root.get("status"), status);
    }
}
