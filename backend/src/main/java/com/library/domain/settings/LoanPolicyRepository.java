package com.library.domain.settings;

import com.library.domain.member.MembershipType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanPolicyRepository extends JpaRepository<LoanPolicy, MembershipType> {
}
