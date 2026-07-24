package com.library.domain.book;

import com.library.common.ApiResponse;
import com.library.domain.book.dto.BookCopyRequest;
import com.library.domain.book.dto.BookCopyResponse;
import com.library.domain.book.dto.BookCopyStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/book-copies")
@RequiredArgsConstructor
@Tag(name = "Book Copies", description = "Quản lý bản sao vật lý")
public class BookCopyController {

    private final BookCopyService service;

    @Operation(summary = "Chi tiết bản sao")
    @GetMapping("/{id}")
    public ApiResponse<BookCopyResponse> get(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @Operation(summary = "Cập nhật bản sao (mã vạch, vị trí kệ, ghi chú)")
    @PutMapping("/{id}")
    public ApiResponse<BookCopyResponse> update(@PathVariable Long id, @Valid @RequestBody BookCopyRequest request) {
        return ApiResponse.success(service.updateCopy(id, request), "Đã cập nhật bản sao");
    }

    @Operation(summary = "Đổi trạng thái bản sao", description = "Chỉ cho phép AVAILABLE, LOST, DAMAGED, MAINTENANCE")
    @PostMapping("/{id}/status")
    public ApiResponse<BookCopyResponse> changeStatus(@PathVariable Long id,
                                                      @Valid @RequestBody BookCopyStatusRequest request) {
        return ApiResponse.success(service.changeStatus(id, request), "Đã cập nhật trạng thái bản sao");
    }

    @Operation(summary = "Xóa bản sao")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.deleteCopy(id);
    }
}
