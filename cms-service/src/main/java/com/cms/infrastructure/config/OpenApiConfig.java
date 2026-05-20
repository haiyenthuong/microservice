package com.cms.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    /**
     * Cấu hình OpenAPI để tài liệu hóa API.
     *
     * @return OpenAPI cấu hình OpenAPI
     */
    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("CMS Service API")
                        .description("CMS Service for Admin Management - Clean Architecture + CQRS + DDD")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("CMS Team")
                                .email("cms@company.com")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Please enter JWT token")))
                .servers(List.of(
                        new Server().url("http://localhost:8081/api").description("Development Server"),
                        new Server().url("https://api.company.com").description("Production Server")
                ));
    }
}
