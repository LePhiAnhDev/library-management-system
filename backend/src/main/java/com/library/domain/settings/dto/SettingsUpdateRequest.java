package com.library.domain.settings.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SettingsUpdateRequest(

        @NotBlank(message = "Tên thư viện không được để trống")
        @Size(max = 255, message = "Tên thư viện tối đa 255 ký tự")
        String libraryName,

        @Size(max = 500, message = "Địa chỉ tối đa 500 ký tự")
        String libraryAddress,

        @NotNull(message = "Đơn giá phạt quá hạn không được để trống")
        @PositiveOrZero(message = "Đơn giá phạt không được âm")
        BigDecimal overdueFinePerDay,

        @NotNull(message = "Ngưỡng phạt chặn mượn không được để trống")
        @PositiveOrZero(message = "Ngưỡng phạt không được âm")
        BigDecimal fineBlockThreshold,

        @NotNull(message = "Thời gian giữ đặt trước không được để trống")
        @Min(value = 1, message = "Thời gian giữ đặt trước phải lớn hơn 0")
        Integer reservationHoldDays,

        @NotNull(message = "Phí sách mất không được để trống")
        @PositiveOrZero(message = "Phí không được âm")
        BigDecimal lostDefaultFee,

        @NotNull(message = "Phí sách hỏng không được để trống")
        @PositiveOrZero(message = "Phí không được âm")
        BigDecimal damagedDefaultFee
) {
}
