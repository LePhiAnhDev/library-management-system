package com.library.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

/**
 * RestClient preconfigured for the Clerk Backend API with the secret key as a Bearer token.
 */
@Configuration
@EnableConfigurationProperties(ClerkProperties.class)
public class ClerkConfig {

    @Bean
    public RestClient clerkRestClient(ClerkProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.apiBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.secretKey())
                .build();
    }
}
