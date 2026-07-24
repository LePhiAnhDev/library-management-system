package com.library.storage;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

/**
 * S3 client pointed at the Cloudflare R2 endpoint. Path style access and disabled chunked encoding
 * match what R2 expects; region is the R2 "auto" value.
 */
@Configuration
@EnableConfigurationProperties(R2Properties.class)
public class StorageConfig {

    @Bean
    public S3Client r2Client(R2Properties properties) {
        return S3Client.builder()
                .endpointOverride(URI.create("https://" + properties.accountId() + ".r2.cloudflarestorage.com"))
                .region(Region.of(properties.region()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.accessKeyId(), properties.secretAccessKey())))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .chunkedEncodingEnabled(false)
                        .build())
                .build();
    }
}
