package com.library.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables JPA auditing so BaseEntity.createdAt / updatedAt are populated automatically.
 * The auditor (createdBy) is wired in once the user domain exists.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
