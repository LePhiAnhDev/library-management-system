package com.library.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Clerk backend integration settings (used to fetch staff profile details on provisioning).
 */
@ConfigurationProperties(prefix = "app.clerk")
public record ClerkProperties(String secretKey, String apiBaseUrl, String issuerUri, String jwksUri) {
}
