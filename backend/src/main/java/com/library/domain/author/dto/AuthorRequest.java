package com.library.domain.author.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthorRequest(

        @NotBlank(message = "Tên tác giả không được để trống")
        @Size(max = 255, message = "Tên tác giả tối đa 255 ký tự")
        String fullName,

        @Size(max = 2000, message = "Tiểu sử tối đa 2000 ký tự")
        String biography
) {
}
