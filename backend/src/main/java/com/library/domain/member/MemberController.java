package com.library.domain.member;

import com.library.common.ApiResponse;
import com.library.common.PageResponse;
import com.library.domain.member.dto.MemberProfileResponse;
import com.library.domain.member.dto.MemberRequest;
import com.library.domain.member.dto.MemberResponse;
import com.library.domain.member.dto.MemberStatusRequest;
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
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Tag(name = "Members", description = "Quản lý độc giả")
public class MemberController {

    private final MemberService service;
    private final MemberProfileService profileService;

    @Operation(summary = "Hồ sơ độc giả", description = "Đang mượn, phạt chưa thu, đặt trước đang chờ")
    @GetMapping("/{id}/profile")
    public ApiResponse<MemberProfileResponse> profile(@PathVariable Long id) {
        return ApiResponse.success(profileService.profile(id));
    }

    @Operation(summary = "Tạo độc giả", description = "Mã độc giả được sinh tự động")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MemberResponse> create(@Valid @RequestBody MemberRequest request) {
        return ApiResponse.success(service.create(request), "Đã tạo độc giả");
    }

    @Operation(summary = "Chi tiết độc giả")
    @GetMapping("/{id}")
    public ApiResponse<MemberResponse> get(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @Operation(summary = "Danh sách độc giả", description = "Tìm theo tên/mã/điện thoại/email, lọc loại thẻ và trạng thái")
    @GetMapping
    public ApiResponse<PageResponse<MemberResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) MembershipType membershipType,
            @RequestParam(required = false) MemberStatus status,
            @ParameterObject @PageableDefault(sort = "fullName") Pageable pageable) {
        return ApiResponse.success(service.list(search, membershipType, status, pageable));
    }

    @Operation(summary = "Cập nhật độc giả")
    @PutMapping("/{id}")
    public ApiResponse<MemberResponse> update(@PathVariable Long id, @Valid @RequestBody MemberRequest request) {
        return ApiResponse.success(service.update(id, request), "Đã cập nhật độc giả");
    }

    @Operation(summary = "Đổi trạng thái độc giả")
    @PostMapping("/{id}/status")
    public ApiResponse<MemberResponse> changeStatus(@PathVariable Long id, @Valid @RequestBody MemberStatusRequest request) {
        return ApiResponse.success(service.changeStatus(id, request.status()), "Đã cập nhật trạng thái độc giả");
    }

    @Operation(summary = "Xóa độc giả")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
