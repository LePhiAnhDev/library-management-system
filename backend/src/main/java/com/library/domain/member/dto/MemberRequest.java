package com.library.domain.member.dto;

import com.library.domain.member.MembershipType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Create or update payload for a reader. member_code is generated; joinDate is set on create.
 * expiryDate is optional (defaults to one year from the join date on create).
 */
public record MemberRequest(

        @NotBlank(message = "Họ tên không được để trống")
        @Size(max = 255, message = "Họ tên tối đa 255 ký tự")
        String fullName,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        @Size(max = 320, message = "Email tối đa 320 ký tự")
        String email,

        @Size(max = 30, message = "Số điện thoại tối đa 30 ký tự")
        String phone,

        @Size(max = 500, message = "Địa chỉ tối đa 500 ký tự")
        String address,

        @NotNull(message = "Loại thẻ không được để trống")
        MembershipType membershipType,

        LocalDate expiryDate
) {
}
