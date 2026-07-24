package com.library.domain.publisher;

import com.library.common.ApiResponse;
import com.library.common.PageResponse;
import com.library.common.RecordStatus;
import com.library.domain.publisher.dto.PublisherRequest;
import com.library.domain.publisher.dto.PublisherResponse;
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
@RequestMapping("/api/v1/publishers")
@RequiredArgsConstructor
@Tag(name = "Publishers", description = "Quản lý nhà xuất bản")
public class PublisherController {

    private final PublisherService service;

    @Operation(summary = "Tạo nhà xuất bản")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PublisherResponse> create(@Valid @RequestBody PublisherRequest request) {
        return ApiResponse.success(service.create(request), "Đã tạo nhà xuất bản");
    }

    @Operation(summary = "Chi tiết nhà xuất bản")
    @GetMapping("/{id}")
    public ApiResponse<PublisherResponse> get(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @Operation(summary = "Danh sách nhà xuất bản", description = "Phân trang, tìm kiếm theo tên, lọc theo trạng thái")
    @GetMapping
    public ApiResponse<PageResponse<PublisherResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) RecordStatus status,
            @ParameterObject @PageableDefault(sort = "name") Pageable pageable) {
        return ApiResponse.success(service.list(search, status, pageable));
    }

    @Operation(summary = "Cập nhật nhà xuất bản")
    @PutMapping("/{id}")
    public ApiResponse<PublisherResponse> update(@PathVariable Long id, @Valid @RequestBody PublisherRequest request) {
        return ApiResponse.success(service.update(id, request), "Đã cập nhật nhà xuất bản");
    }

    @Operation(summary = "Xóa mềm nhà xuất bản")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
