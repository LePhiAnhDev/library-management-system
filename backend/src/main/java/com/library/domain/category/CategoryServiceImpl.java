package com.library.domain.category;

import com.library.common.PageResponse;
import com.library.common.RecordStatus;
import com.library.domain.category.dto.CategoryRequest;
import com.library.domain.category.dto.CategoryResponse;
import com.library.exception.BusinessRuleException;
import com.library.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;
    private final CategoryMapper mapper;

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        Category entity = new Category();
        mapper.updateEntity(request, entity);
        entity.setStatus(RecordStatus.ACTIVE);
        entity.setParent(resolveParent(request.parentId(), null));
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category entity = getEntity(id);
        mapper.updateEntity(request, entity);
        entity.setParent(resolveParent(request.parentId(), id));
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        return mapper.toResponse(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> list(String search, RecordStatus status, Long parentId, Pageable pageable) {
        List<Specification<Category>> specs = new ArrayList<>();
        if (StringUtils.hasText(search)) {
            specs.add(CategorySpecifications.nameContains(search));
        }
        if (status != null) {
            specs.add(CategorySpecifications.hasStatus(status));
        }
        if (parentId != null) {
            specs.add(CategorySpecifications.hasParent(parentId));
        }
        Page<CategoryResponse> page = repository.findAll(Specification.allOf(specs), pageable).map(mapper::toResponse);
        return PageResponse.from(page);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category entity = getEntity(id);
        if (repository.existsByParentId(id)) {
            throw new BusinessRuleException("Không thể xóa thể loại đang có thể loại con");
        }
        // Soft delete: preserve the record for any books that reference it historically.
        entity.setStatus(RecordStatus.INACTIVE);
        repository.save(entity);
    }

    private Category getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("thể loại", id));
    }

    /**
     * Resolves the parent category, rejecting a self reference or any cycle in the hierarchy.
     */
    private Category resolveParent(Long parentId, Long selfId) {
        if (parentId == null) {
            return null;
        }
        if (selfId != null && parentId.equals(selfId)) {
            throw new BusinessRuleException("Thể loại không thể là cha của chính nó");
        }
        Category parent = repository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thể loại cha với id " + parentId));
        if (selfId != null) {
            for (Category cursor = parent; cursor != null; cursor = cursor.getParent()) {
                if (selfId.equals(cursor.getId())) {
                    throw new BusinessRuleException("Không thể đặt thể loại con làm cha vì sẽ tạo vòng lặp");
                }
            }
        }
        return parent;
    }
}
