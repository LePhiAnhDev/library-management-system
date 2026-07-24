package com.library.domain.publisher.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PublisherRequest(

        @NotBlank(message = "Tên nhà xuất bản không được để trống")
        @Size(max = 255, message = "Tên nhà xuất bản tối đa 255 ký tự")
        String name,

        @Size(max = 500, message = "Địa chỉ tối đa 500 ký tự")
        String address,

        @Size(max = 30, message = "Số điện thoại tối đa 30 ký tự")
        String phone,

        @Email(message = "Email không hợp lệ")
        @Size(max = 320, message = "Email tối đa 320 ký tự")
        String email
) {
}
