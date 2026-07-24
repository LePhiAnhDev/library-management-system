package com.library;

import com.library.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

/**
 * Smoke test: the full context boots and all Flyway migrations apply against a fresh database.
 */
class LibraryApplicationTests extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
    }
}
