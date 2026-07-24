package com.library.domain.author;

import com.library.common.ApiResponse;
import com.library.common.PageResponse;
import com.library.common.RecordStatus;
import com.library.domain.author.dto.AuthorRequest;
import com.library.domain.author.dto.AuthorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/authors")
@RequiredArgsConstructor
@Tag(name = "Authors", description = "Quản lý tác giả")
public class AuthorController {

    private final AuthorService service;

    @Operation(summary = "Tạo tác giả")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthorResponse> create(@Valid @RequestBody AuthorRequest request) {
        return ApiResponse.success(service.create(request), "Đã tạo tác giả");
    }

    @Operation(summary = "Chi tiết tác giả")
    @GetMapping("/{id}")
    public ApiResponse<AuthorResponse> get(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @Operation(summary = "Danh sách tác giả", description = "Phân trang, tìm kiếm theo tên, lọc theo trạng thái")
    @GetMapping
    public ApiResponse<PageResponse<AuthorResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) RecordStatus status,
            @ParameterObject @PageableDefault(sort = "fullName") Pageable pageable) {
        return ApiResponse.success(service.list(search, status, pageable));
    }

    @Operation(summary = "Cập nhật tác giả")
    @PutMapping("/{id}")
    public ApiResponse<AuthorResponse> update(@PathVariable Long id, @Valid @RequestBody AuthorRequest request) {
        return ApiResponse.success(service.update(id, request), "Đã cập nhật tác giả");
    }

    @Operation(summary = "Xóa mềm tác giả")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
