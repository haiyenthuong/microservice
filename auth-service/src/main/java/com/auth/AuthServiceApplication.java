package com.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main Application class cho Authentication Service
 *
 * Service này chịu trách nhiệm:
 * - Authentication (login, register, token management)
 * - Authorization (RBAC, permission checking)
 * - User management (CRUD operations)
 * - Role/Group management
 * - Parameter/Config management
 *
 * @author Auth Service Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
