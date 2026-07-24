package com.library.domain.publisher;

import com.library.common.PageResponse;
import com.library.common.RecordStatus;
import com.library.domain.publisher.dto.PublisherRequest;
import com.library.domain.publisher.dto.PublisherResponse;
import org.springframework.data.domain.Pageable;

public interface PublisherService {

    PublisherResponse create(PublisherRequest request);

    PublisherResponse update(Long id, PublisherRequest request);

    PublisherResponse getById(Long id);

    PageResponse<PublisherResponse> list(String search, RecordStatus status, Pageable pageable);

    void delete(Long id);
}
