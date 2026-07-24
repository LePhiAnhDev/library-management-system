package com.library.domain.book.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * Create or update payload for a bibliographic book. Copies are managed separately.
 */
public record BookRequest(

        @NotBlank(message = "ISBN không được để trống")
        @Size(max = 20, message = "ISBN tối đa 20 ký tự")
        String isbn,

        @NotBlank(message = "Tiêu đề không được để trống")
        @Size(max = 500, message = "Tiêu đề tối đa 500 ký tự")
        String title,

        @Size(max = 500, message = "Tiêu đề phụ tối đa 500 ký tự")
        String subtitle,

        @Size(max = 4000, message = "Mô tả tối đa 4000 ký tự")
        String description,

        Long publisherId,

        @NotNull(message = "Thể loại không được để trống")
        Long categoryId,

        Set<Long> authorIds,

        @Min(value = 1000, message = "Năm xuất bản không hợp lệ")
        @Max(value = 2100, message = "Năm xuất bản không hợp lệ")
        Integer publicationYear,

        @Size(max = 50, message = "Ngôn ngữ tối đa 50 ký tự")
        String language,

        @Min(value = 1, message = "Số trang phải lớn hơn 0")
        Integer pageCount,

        @Size(max = 1024, message = "Đường dẫn ảnh bìa tối đa 1024 ký tự")
        String coverImageUrl
) {
}
