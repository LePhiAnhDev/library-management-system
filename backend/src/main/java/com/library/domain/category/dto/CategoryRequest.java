package com.library.domain.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Create or update payload for a category. parentId is optional (null means a top level category).
 */
public record CategoryRequest(

        @NotBlank(message = "Tên thể loại không được để trống")
        @Size(max = 255, message = "Tên thể loại tối đa 255 ký tự")
        String name,

        @Size(max = 2000, message = "Mô tả tối đa 2000 ký tự")
        String description,

        Long parentId
) {
}
