package com.library.domain.book.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Create or update payload for a physical copy. Status is not set here; new copies start AVAILABLE
 * and status changes go through dedicated actions.
 */
public record BookCopyRequest(

        @NotBlank(message = "Mã vạch không được để trống")
        @Size(max = 64, message = "Mã vạch tối đa 64 ký tự")
        String barcode,

        @Size(max = 100, message = "Vị trí kệ tối đa 100 ký tự")
        String shelfLocation,

        LocalDate acquiredDate,

        @Size(max = 1000, message = "Ghi chú tình trạng tối đa 1000 ký tự")
        String conditionNote
) {
}
