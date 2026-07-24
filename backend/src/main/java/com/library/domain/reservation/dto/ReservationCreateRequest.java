package com.library.domain.reservation.dto;

import jakarta.validation.constraints.NotNull;

public record ReservationCreateRequest(

        @NotNull(message = "Độc giả không được để trống")
        Long memberId,

        @NotNull(message = "Đầu sách không được để trống")
        Long bookId
) {
}
