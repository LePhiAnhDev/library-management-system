package com.library.domain.fine;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;

public interface FineRepository extends JpaRepository<Fine, Long>, JpaSpecificationExecutor<Fine> {

    @Query("select coalesce(sum(f.amount), 0) from Fine f where f.member.id = :memberId and f.status = :status")
    BigDecimal sumByMemberAndStatus(@Param("memberId") Long memberId, @Param("status") FineStatus status);

    boolean existsByMemberIdAndStatus(Long memberId, FineStatus status);

    boolean existsByMemberId(Long memberId);

    @Query("select coalesce(sum(f.amount), 0) from Fine f where f.status = :status")
    BigDecimal sumByStatus(@Param("status") FineStatus status);

    @Query("select coalesce(sum(f.amount), 0) from Fine f where f.status = :status and f.paidAt between :from and :to")
    BigDecimal sumByStatusAndPaidAtBetween(@Param("status") FineStatus status,
                                           @Param("from") Instant from, @Param("to") Instant to);
}
