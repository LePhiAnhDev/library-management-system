package com.library.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Cloudflare R2 (S3 compatible) settings for storing book cover images.
 */
@ConfigurationProperties(prefix = "app.storage.r2")
public record R2Properties(
        String accountId,
        String accessKeyId,
        String secretAccessKey,
        String bucketName,
        String region,
        String publicBaseUrl
) {
}
