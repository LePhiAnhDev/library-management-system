package com.library.domain.reservation;

import com.library.common.ApiResponse;
import com.library.common.PageResponse;
import com.library.domain.reservation.dto.ReservationCreateRequest;
import com.library.domain.reservation.dto.ReservationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations", description = "Đặt trước sách")
public class ReservationController {

    private final ReservationService service;

    @Operation(summary = "Đặt trước sách")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReservationResponse> create(@Valid @RequestBody ReservationCreateRequest request) {
        return ApiResponse.success(service.create(request), "Đã đặt trước");
    }

    @Operation(summary = "Hủy đặt trước")
    @PostMapping("/{id}/cancel")
    public ApiResponse<ReservationResponse> cancel(@PathVariable Long id) {
        return ApiResponse.success(service.cancel(id), "Đã hủy đặt trước");
    }

    @Operation(summary = "Chi tiết đặt trước")
    @GetMapping("/{id}")
    public ApiResponse<ReservationResponse> get(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @Operation(summary = "Hàng chờ đặt trước theo đầu sách")
    @GetMapping("/queue")
    public ApiResponse<List<ReservationResponse>> queue(@RequestParam Long bookId) {
        return ApiResponse.success(service.queueForBook(bookId));
    }

    @Operation(summary = "Danh sách đặt trước", description = "Lọc theo đầu sách, độc giả và trạng thái")
    @GetMapping
    public ApiResponse<PageResponse<ReservationResponse>> list(
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) ReservationStatus status,
            @ParameterObject @PageableDefault(sort = "createdAt") Pageable pageable) {
        return ApiResponse.success(service.list(bookId, memberId, status, pageable));
    }
}
