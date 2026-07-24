package com.library.domain.category;

import com.library.common.RecordStatus;
import org.springframework.data.jpa.domain.Specification;

/**
 * Dynamic filters for the category list endpoint (Specification pattern).
 */
public final class CategorySpecifications {

    private CategorySpecifications() {
    }

    public static Specification<Category> nameContains(String query) {
        String pattern = "%" + query.trim().toLowerCase() + "%";
        return (root, criteria, cb) -> cb.like(cb.lower(root.get("name")), pattern);
    }

    public static Specification<Category> hasStatus(RecordStatus status) {
        return (root, criteria, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Category> hasParent(Long parentId) {
        return (root, criteria, cb) -> cb.equal(root.get("parent").get("id"), parentId);
    }
}
