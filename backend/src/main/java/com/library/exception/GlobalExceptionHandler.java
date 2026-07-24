package com.library.exception;

import com.library.common.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

/**
 * Central translation of exceptions into the ApiResponse envelope with a stable errorCode
 * and the correct HTTP status. Stack traces are never sent to clients.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Object>> handleApi(ApiException ex) {
        log.warn("Business exception [{}]: {}", ex.getErrorCode(), ex.getMessage());
        return build(ex.getErrorCode(), ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleBodyValidation(MethodArgumentNotValidException ex) {
        List<ValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ValidationError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        return build(ErrorCode.VALIDATION_ERROR, "Dữ liệu không hợp lệ", errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleParamValidation(ConstraintViolationException ex) {
        List<ValidationError> errors = ex.getConstraintViolations().stream()
                .map(v -> new ValidationError(lastPathNode(v.getPropertyPath().toString()), v.getMessage()))
                .toList();
        return build(ErrorCode.VALIDATION_ERROR, "Dữ liệu không hợp lệ", errors);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ApiResponse<Object>> handleMalformedRequest(Exception ex) {
        log.warn("Malformed request: {}", ex.getMessage());
        return build(ErrorCode.BAD_REQUEST, "Yêu cầu không hợp lệ hoặc thiếu tham số", null);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadSort(PropertyReferenceException ex) {
        log.warn("Invalid sort/property reference: {}", ex.getMessage());
        return build(ErrorCode.BAD_REQUEST, "Trường sắp xếp hoặc lọc không hợp lệ", null);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Object>> handleOptimisticLock(OptimisticLockingFailureException ex) {
        log.warn("Optimistic lock conflict: {}", ex.getMessage());
        return build(ErrorCode.OPTIMISTIC_LOCK_CONFLICT,
                "Dữ liệu vừa được thay đổi bởi một thao tác khác, vui lòng tải lại và thử lại", null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        return build(ErrorCode.CONFLICT, "Thao tác vi phạm ràng buộc dữ liệu", null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(AccessDeniedException ex) {
        return build(ErrorCode.ACCESS_DENIED, "Bạn không có quyền thực hiện thao tác này", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return build(ErrorCode.INTERNAL_ERROR, "Đã xảy ra lỗi không mong muốn", null);
    }

    private ResponseEntity<ApiResponse<Object>> build(ErrorCode code, String message, Object data) {
        ApiResponse<Object> body = (data == null)
                ? ApiResponse.error(message, code.name())
                : ApiResponse.error(message, code.name(), data);
        return ResponseEntity.status(code.status()).body(body);
    }

    private String lastPathNode(String propertyPath) {
        int idx = propertyPath.lastIndexOf('.');
        return idx >= 0 ? propertyPath.substring(idx + 1) : propertyPath;
    }
}
