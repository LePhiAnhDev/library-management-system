package com.library.domain.book;

import com.library.common.ApiResponse;
import com.library.common.PageResponse;
import com.library.common.RecordStatus;
import com.library.domain.book.dto.BookCopyRequest;
import com.library.domain.book.dto.BookCopyResponse;
import com.library.domain.book.dto.BookRequest;
import com.library.domain.book.dto.BookResponse;
import com.library.storage.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@Tag(name = "Books", description = "Quản lý đầu sách và bản sao")
public class BookController {

    private final BookService bookService;
    private final BookCopyService bookCopyService;
    private final StorageService storageService;

    @Operation(summary = "Tạo đầu sách")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BookResponse> create(@Valid @RequestBody BookRequest request) {
        return ApiResponse.success(bookService.create(request), "Đã tạo đầu sách");
    }

    @Operation(summary = "Chi tiết đầu sách")
    @GetMapping("/{id}")
    public ApiResponse<BookResponse> get(@PathVariable Long id) {
        return ApiResponse.success(bookService.getById(id));
    }

    @Operation(summary = "Danh sách đầu sách",
            description = "Tìm kiếm theo tiêu đề/ISBN, lọc theo thể loại, tác giả, nhà xuất bản, tình trạng còn sách và trạng thái")
    @GetMapping
    public ApiResponse<PageResponse<BookResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long publisherId,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) RecordStatus status,
            @ParameterObject @PageableDefault(sort = "title") Pageable pageable) {
        return ApiResponse.success(
                bookService.list(search, categoryId, publisherId, authorId, available, status, pageable));
    }

    @Operation(summary = "Cập nhật đầu sách")
    @PutMapping("/{id}")
    public ApiResponse<BookResponse> update(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        return ApiResponse.success(bookService.update(id, request), "Đã cập nhật đầu sách");
    }

    @Operation(summary = "Xóa mềm đầu sách")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        bookService.delete(id);
    }

    @Operation(summary = "Danh sách bản sao của đầu sách")
    @GetMapping("/{id}/copies")
    public ApiResponse<List<BookCopyResponse>> listCopies(@PathVariable Long id) {
        return ApiResponse.success(bookCopyService.listByBook(id));
    }

    @Operation(summary = "Thêm bản sao cho đầu sách")
    @PostMapping("/{id}/copies")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BookCopyResponse> addCopy(@PathVariable Long id, @Valid @RequestBody BookCopyRequest request) {
        return ApiResponse.success(bookCopyService.addCopy(id, request), "Đã thêm bản sao");
    }

    @Operation(summary = "Tải ảnh bìa cho đầu sách")
    @PostMapping(value = "/{id}/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<BookResponse> uploadCover(@PathVariable Long id,
                                                 @RequestParam("file") MultipartFile file) {
        String url = storageService.uploadCover(id, file);
        return ApiResponse.success(bookService.updateCoverImage(id, url), "Đã tải ảnh bìa");
    }
}
