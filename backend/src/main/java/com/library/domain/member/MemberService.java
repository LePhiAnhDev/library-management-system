package com.library.domain.member;

import com.library.common.PageResponse;
import com.library.domain.member.dto.MemberRequest;
import com.library.domain.member.dto.MemberResponse;
import org.springframework.data.domain.Pageable;

public interface MemberService {

    MemberResponse create(MemberRequest request);

    MemberResponse update(Long id, MemberRequest request);

    MemberResponse changeStatus(Long id, MemberStatus status);

    MemberResponse getById(Long id);

    PageResponse<MemberResponse> list(String search, MembershipType membershipType, MemberStatus status, Pageable pageable);

    void delete(Long id);
}
