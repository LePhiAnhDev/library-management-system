package com.library.domain.author;

import com.library.common.PageResponse;
import com.library.common.RecordStatus;
import com.library.domain.author.dto.AuthorRequest;
import com.library.domain.author.dto.AuthorResponse;
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
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository repository;
    private final AuthorMapper mapper;

    @Override
    @Transactional
    public AuthorResponse create(AuthorRequest request) {
        Author entity = new Author();
        mapper.updateEntity(request, entity);
        entity.setStatus(RecordStatus.ACTIVE);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public AuthorResponse update(Long id, AuthorRequest request) {
        Author entity = getEntity(id);
        mapper.updateEntity(request, entity);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthorResponse getById(Long id) {
        return mapper.toResponse(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuthorResponse> list(String search, RecordStatus status, Pageable pageable) {
        List<Specification<Author>> specs = new ArrayList<>();
        if (StringUtils.hasText(search)) {
            specs.add(AuthorSpecifications.nameContains(search));
        }
        if (status != null) {
            specs.add(AuthorSpecifications.hasStatus(status));
        }
        Page<AuthorResponse> page = repository.findAll(Specification.allOf(specs), pageable).map(mapper::toResponse);
        return PageResponse.from(page);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Author entity = getEntity(id);
        // Soft delete keeps the author for books that reference it. Book reference guard is
        // enforced once the catalog exists (books cannot leave an author dangling).
        entity.setStatus(RecordStatus.INACTIVE);
        repository.save(entity);
    }

    private Author getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("tác giả", id));
    }
}
