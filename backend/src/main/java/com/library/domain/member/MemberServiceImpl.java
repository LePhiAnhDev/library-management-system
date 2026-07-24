package com.library.domain.member;

import com.library.common.PageResponse;
import com.library.domain.fine.FineRepository;
import com.library.domain.loan.LoanRepository;
import com.library.domain.member.dto.MemberRequest;
import com.library.domain.member.dto.MemberResponse;
import com.library.exception.BusinessRuleException;
import com.library.exception.DuplicateResourceException;
import com.library.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository repository;
    private final MemberMapper mapper;
    private final LoanRepository loanRepository;
    private final FineRepository fineRepository;

    @Override
    @Transactional
    public MemberResponse create(MemberRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email đã tồn tại: " + request.email());
        }
        Member member = new Member();
        mapper.updateEntity(request, member);
        member.setMemberCode(generateMemberCode());
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        member.setJoinDate(today);
        member.setExpiryDate(request.expiryDate() != null ? request.expiryDate() : today.plusYears(1));
        member.setStatus(MemberStatus.ACTIVE);
        return mapper.toResponse(repository.save(member));
    }

    @Override
    @Transactional
    public MemberResponse update(Long id, MemberRequest request) {
        Member member = getEntity(id);
        if (repository.existsByEmailAndIdNot(request.email(), id)) {
            throw new DuplicateResourceException("Email đã tồn tại: " + request.email());
        }
        mapper.updateEntity(request, member);
        if (request.expiryDate() != null) {
            member.setExpiryDate(request.expiryDate());
        }
        return mapper.toResponse(repository.save(member));
    }

    @Override
    @Transactional
    public MemberResponse changeStatus(Long id, MemberStatus status) {
        Member member = getEntity(id);
        member.setStatus(status);
        return mapper.toResponse(repository.save(member));
    }

    @Override
    @Transactional(readOnly = true)
    public MemberResponse getById(Long id) {
        return mapper.toResponse(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MemberResponse> list(String search, MembershipType membershipType, MemberStatus status,
                                             Pageable pageable) {
        List<Specification<Member>> specs = new ArrayList<>();
        if (StringUtils.hasText(search)) {
            specs.add(MemberSpecifications.keyword(search));
        }
        if (membershipType != null) {
            specs.add(MemberSpecifications.hasMembershipType(membershipType));
        }
        if (status != null) {
            specs.add(MemberSpecifications.hasStatus(status));
        }
        Page<MemberResponse> page = repository.findAll(Specification.allOf(specs), pageable).map(mapper::toResponse);
        return PageResponse.from(page);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Member member = getEntity(id);
        // Never hard delete a member that has transactions; only clean, unreferenced records go.
        if (loanRepository.existsByMemberId(id)) {
            throw new BusinessRuleException("Không thể xóa độc giả đã có lịch sử mượn");
        }
        if (fineRepository.existsByMemberId(id)) {
            throw new BusinessRuleException("Không thể xóa độc giả đang có phạt");
        }
        repository.delete(member);
    }

    private Member getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("độc giả", id));
    }

    private String generateMemberCode() {
        long sequence = repository.nextMemberCodeSequence();
        return "MB" + String.format("%06d", sequence);
    }
}
