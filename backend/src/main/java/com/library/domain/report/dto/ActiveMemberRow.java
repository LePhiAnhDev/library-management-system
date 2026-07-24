package com.library.domain.report.dto;

public record ActiveMemberRow(Long memberId, String memberCode, String fullName, long loanCount) {
}
