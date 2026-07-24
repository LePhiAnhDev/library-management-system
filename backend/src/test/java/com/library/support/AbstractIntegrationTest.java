package com.library.support;

import com.library.security.clerk.ClerkUserClient;
import com.library.security.clerk.ClerkUserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

/**
 * Base for integration tests. Uses a single PostgreSQL container started once for the whole JVM
 * (the Testcontainers singleton pattern) so the cached Spring context stays valid across every
 * test class. Endpoints are exercised through MockMvc with a mock Clerk JWT; the Clerk Backend
 * API client is stubbed so provisioning is hermetic and offline.
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected ClerkUserClient clerkUserClient;

    @BeforeEach
    void stubClerkUser() {
        when(clerkUserClient.fetchUser(anyString()))
                .thenReturn(Optional.of(new ClerkUserInfo("staff@library.local", "Nhân viên Thư viện", null)));
    }

    /**
     * A mock authenticated staff member. Any authenticated staff may call any endpoint (no roles).
     */
    protected static JwtRequestPostProcessor staffJwt() {
        return jwt().jwt(builder -> builder.subject("user_test_staff").claim("email", "staff@library.local"));
    }
}
