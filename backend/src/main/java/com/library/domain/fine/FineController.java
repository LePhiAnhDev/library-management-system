package com.library.domain.fine;

import com.library.common.ApiResponse;
import com.library.common.PageResponse;
import com.library.domain.fine.dto.FineCreateRequest;
import com.library.domain.fine.dto.FineResponse;
import com.library.domain.fine.dto.FineWaiveRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/fines")
@RequiredArgsConstructor
@Tag(name = "Fines", description = "Quản lý phạt")
public class FineController {

    private final FineService service;

    @Operation(summary = "Tạo phạt thủ công")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FineResponse> create(@Valid @RequestBody FineCreateRequest request) {
        return ApiResponse.success(service.createManual(request), "Đã tạo phạt");
    }

    @Operation(summary = "Chi tiết phạt")
    @GetMapping("/{id}")
    public ApiResponse<FineResponse> get(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @Operation(summary = "Danh sách phạt", description = "Lọc theo độc giả, loại, trạng thái và khoảng thời gian")
    @GetMapping
    public ApiResponse<PageResponse<FineResponse>> list(
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) FineType type,
            @RequestParam(required = false) FineStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @ParameterObject @PageableDefault(sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.success(service.list(memberId, type, status, from, to, pageable));
    }

    @Operation(summary = "Thu phạt", description = "Idempotent: thu lại phạt đã thu không tạo bản ghi trùng")
    @PostMapping("/{id}/settle")
    public ApiResponse<FineResponse> settle(@PathVariable Long id) {
        return ApiResponse.success(service.settle(id), "Đã thu phạt");
    }

    @Operation(summary = "Miễn phạt")
    @PostMapping("/{id}/waive")
    public ApiResponse<FineResponse> waive(@PathVariable Long id, @Valid @RequestBody FineWaiveRequest request) {
        return ApiResponse.success(service.waive(id, request.reason()), "Đã miễn phạt");
    }
}
