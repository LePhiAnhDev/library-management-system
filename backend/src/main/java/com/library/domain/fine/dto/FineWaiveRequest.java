package com.library.domain.fine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FineWaiveRequest(

        @NotBlank(message = "Lý do miễn phạt không được để trống")
        @Size(max = 500, message = "Lý do tối đa 500 ký tự")
        String reason
) {
}
