package com.library.domain.settings;

import com.library.common.ApiResponse;
import com.library.domain.member.MembershipType;
import com.library.domain.settings.dto.LoanPolicyResponse;
import com.library.domain.settings.dto.LoanPolicyUpdateRequest;
import com.library.domain.settings.dto.SettingsResponse;
import com.library.domain.settings.dto.SettingsUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
@Tag(name = "Settings", description = "Cấu hình hệ thống thư viện")
public class SettingsController {

    private final SettingsService service;

    @Operation(summary = "Xem cấu hình hệ thống")
    @GetMapping
    public ApiResponse<SettingsResponse> get() {
        return ApiResponse.success(service.getSettings());
    }

    @Operation(summary = "Cập nhật cấu hình thư viện")
    @PutMapping
    public ApiResponse<SettingsResponse> update(@Valid @RequestBody SettingsUpdateRequest request) {
        return ApiResponse.success(service.updateSettings(request), "Đã cập nhật cấu hình");
    }

    @Operation(summary = "Cập nhật chính sách mượn theo loại thẻ")
    @PutMapping("/loan-policies/{membershipType}")
    public ApiResponse<LoanPolicyResponse> updateLoanPolicy(@PathVariable MembershipType membershipType,
                                                            @Valid @RequestBody LoanPolicyUpdateRequest request) {
        return ApiResponse.success(service.updateLoanPolicy(membershipType, request), "Đã cập nhật chính sách mượn");
    }
}
