package com.library.domain.author;

import com.library.common.PageResponse;
import com.library.common.RecordStatus;
import com.library.domain.author.dto.AuthorRequest;
import com.library.domain.author.dto.AuthorResponse;
import org.springframework.data.domain.Pageable;

public interface AuthorService {

    AuthorResponse create(AuthorRequest request);

    AuthorResponse update(Long id, AuthorRequest request);

    AuthorResponse getById(Long id);

    PageResponse<AuthorResponse> list(String search, RecordStatus status, Pageable pageable);

    void delete(Long id);
}
