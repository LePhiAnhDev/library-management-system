package com.library.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables the application's scheduled tasks (e.g. reservation hold expiry).
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
