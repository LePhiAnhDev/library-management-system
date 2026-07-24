package com.library.domain.fine;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface FineRepository extends JpaRepository<Fine, Long>, JpaSpecificationExecutor<Fine> {

    @Query("select coalesce(sum(f.amount), 0) from Fine f where f.member.id = :memberId and f.status = :status")
    BigDecimal sumByMemberAndStatus(@Param("memberId") Long memberId, @Param("status") FineStatus status);

    boolean existsByMemberIdAndStatus(Long memberId, FineStatus status);

    boolean existsByMemberId(Long memberId);
}
