package com.library.domain.category;

import com.library.common.PageResponse;
import com.library.common.RecordStatus;
import com.library.domain.category.dto.CategoryRequest;
import com.library.domain.category.dto.CategoryResponse;
import org.springframework.data.domain.Pageable;

public interface CategoryService {

    CategoryResponse create(CategoryRequest request);

    CategoryResponse update(Long id, CategoryRequest request);

    CategoryResponse getById(Long id);

    PageResponse<CategoryResponse> list(String search, RecordStatus status, Long parentId, Pageable pageable);

    void delete(Long id);
}
