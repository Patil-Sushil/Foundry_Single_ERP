package com.kalibyte.foundry.config;

import org.springframework.context.annotation.Configuration;

/**
 * Flyway configuration for PUBLIC (shared) schema only.
 * Tenant schemas are migrated programmatically.
 */
@Configuration
public class FlywayConfig {
    // Intentionally empty.
    // Configuration is driven by application.yaml
}
