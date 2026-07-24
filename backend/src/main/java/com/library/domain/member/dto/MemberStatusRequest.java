package com.library.domain.member.dto;

import com.library.domain.member.MemberStatus;
import jakarta.validation.constraints.NotNull;

public record MemberStatusRequest(

        @NotNull(message = "Trạng thái không được để trống")
        MemberStatus status
) {
}
