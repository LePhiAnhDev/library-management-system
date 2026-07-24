package com.library.domain.settings.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record LoanPolicyUpdateRequest(

        @NotNull(message = "Số sách tối đa không được để trống")
        @Min(value = 1, message = "Số sách tối đa phải lớn hơn 0")
        Integer maxBooks,

        @NotNull(message = "Thời hạn mượn không được để trống")
        @Min(value = 1, message = "Thời hạn mượn phải lớn hơn 0")
        Integer loanPeriodDays,

        @NotNull(message = "Số lần gia hạn không được để trống")
        @Min(value = 0, message = "Số lần gia hạn không được âm")
        Integer maxRenewals
) {
}
