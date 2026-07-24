package com.library.storage;

import com.library.exception.BusinessRuleException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * Uploads book cover images to R2 and returns their public URL.
 */
@Service
@RequiredArgsConstructor
public class StorageService {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    private final S3Client r2Client;
    private final R2Properties properties;

    public String uploadCover(Long bookId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("Tệp ảnh không được để trống");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessRuleException("Chỉ chấp nhận ảnh JPEG, PNG, WEBP hoặc GIF");
        }
        String key = "covers/" + bookId + "/" + UUID.randomUUID() + extensionFor(contentType);
        try {
            r2Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(properties.bucketName())
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new StorageException("Không đọc được tệp ảnh khi tải lên", e);
        } catch (RuntimeException e) {
            throw new StorageException("Không thể tải ảnh lên object storage", e);
        }
        return properties.publicBaseUrl() + "/" + key;
    }

    private String extensionFor(String contentType) {
        return switch (contentType.toLowerCase()) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".jpg";
        };
    }
}
