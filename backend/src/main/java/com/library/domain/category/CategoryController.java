package com.library.domain.category;

import com.library.common.ApiResponse;
import com.library.common.PageResponse;
import com.library.common.RecordStatus;
import com.library.domain.category.dto.CategoryRequest;
import com.library.domain.category.dto.CategoryResponse;
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
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Quản lý thể loại sách")
public class CategoryController {

    private final CategoryService service;

    @Operation(summary = "Tạo thể loại")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        return ApiResponse.success(service.create(request), "Đã tạo thể loại");
    }

    @Operation(summary = "Chi tiết thể loại")
    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> get(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @Operation(summary = "Danh sách thể loại", description = "Phân trang, tìm kiếm theo tên, lọc theo trạng thái và thể loại cha")
    @GetMapping
    public ApiResponse<PageResponse<CategoryResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) RecordStatus status,
            @RequestParam(required = false) Long parentId,
            @ParameterObject @PageableDefault(sort = "name") Pageable pageable) {
        return ApiResponse.success(service.list(search, status, parentId, pageable));
    }

    @Operation(summary = "Cập nhật thể loại")
    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return ApiResponse.success(service.update(id, request), "Đã cập nhật thể loại");
    }

    @Operation(summary = "Xóa mềm thể loại")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
