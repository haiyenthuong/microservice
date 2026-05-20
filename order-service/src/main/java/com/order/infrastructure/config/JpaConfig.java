package com.order.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuration class cho JPA.
 * Bật auditing để tự động populate createdDate, updatedDate, createdBy, updatedBy.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
