package com.library.domain.loan;

import com.library.common.ApiResponse;
import com.library.common.PageResponse;
import com.library.domain.loan.dto.CheckoutRequest;
import com.library.domain.loan.dto.LoanResponse;
import com.library.domain.loan.dto.ReturnByBarcodeRequest;
import com.library.domain.loan.dto.ReturnRequest;
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
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
@Tag(name = "Loans", description = "Cho mượn, gia hạn, trả sách")
public class LoanController {

    private final LoanService service;

    @Operation(summary = "Cho mượn", description = "Chọn độc giả và bản sao (theo id hoặc mã vạch)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<LoanResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        return ApiResponse.success(service.checkout(request), "Đã cho mượn");
    }

    @Operation(summary = "Gia hạn phiếu mượn")
    @PostMapping("/{id}/renew")
    public ApiResponse<LoanResponse> renew(@PathVariable Long id) {
        return ApiResponse.success(service.renew(id), "Đã gia hạn");
    }

    @Operation(summary = "Trả sách theo phiếu mượn")
    @PostMapping("/{id}/return")
    public ApiResponse<LoanResponse> returnLoan(@PathVariable Long id,
                                                @RequestBody(required = false) ReturnRequest request) {
        ReturnRequest effective = request != null ? request : new ReturnRequest(null, null, null);
        return ApiResponse.success(service.returnLoan(id, effective), "Đã trả sách");
    }

    @Operation(summary = "Trả sách theo mã vạch")
    @PostMapping("/return")
    public ApiResponse<LoanResponse> returnByBarcode(@Valid @RequestBody ReturnByBarcodeRequest request) {
        return ApiResponse.success(service.returnByBarcode(request), "Đã trả sách");
    }

    @Operation(summary = "Chi tiết phiếu mượn")
    @GetMapping("/{id}")
    public ApiResponse<LoanResponse> get(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @Operation(summary = "Danh sách phiếu mượn",
            description = "Tìm theo mã, lọc theo độc giả, trạng thái (BORROWED/RETURNED/OVERDUE) và khoảng ngày mượn")
    @GetMapping
    public ApiResponse<PageResponse<LoanResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) LoanStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @ParameterObject @PageableDefault(sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.success(service.list(search, memberId, status, from, to, pageable));
    }
}
