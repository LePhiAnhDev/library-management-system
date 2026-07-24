package com.library.domain.publisher;

import com.library.common.PageResponse;
import com.library.common.RecordStatus;
import com.library.domain.publisher.dto.PublisherRequest;
import com.library.domain.book.BookRepository;
import com.library.domain.publisher.dto.PublisherResponse;
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
public class PublisherServiceImpl implements PublisherService {

    private final PublisherRepository repository;
    private final PublisherMapper mapper;
    private final BookRepository bookRepository;

    @Override
    @Transactional
    public PublisherResponse create(PublisherRequest request) {
        Publisher entity = new Publisher();
        mapper.updateEntity(request, entity);
        entity.setStatus(RecordStatus.ACTIVE);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public PublisherResponse update(Long id, PublisherRequest request) {
        Publisher entity = getEntity(id);
        mapper.updateEntity(request, entity);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public PublisherResponse getById(Long id) {
        return mapper.toResponse(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PublisherResponse> list(String search, RecordStatus status, Pageable pageable) {
        List<Specification<Publisher>> specs = new ArrayList<>();
        if (StringUtils.hasText(search)) {
            specs.add(PublisherSpecifications.nameContains(search));
        }
        if (status != null) {
            specs.add(PublisherSpecifications.hasStatus(status));
        }
        Page<PublisherResponse> page = repository.findAll(Specification.allOf(specs), pageable).map(mapper::toResponse);
        return PageResponse.from(page);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Publisher entity = getEntity(id);
        if (bookRepository.existsByPublisherId(id)) {
            throw new BusinessRuleException("Không thể xóa nhà xuất bản đang có sách");
        }
        entity.setStatus(RecordStatus.INACTIVE);
        repository.save(entity);
    }

    private Publisher getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("nhà xuất bản", id));
    }
}
