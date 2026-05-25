package com.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Refresh Token Response DTO
 *
 * Chứa thông tin sau khi refresh token thành công
 *
 * @author Auth Service
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Refresh token response")
public class RefreshTokenResponse {

    @Schema(description = "New JWT access token")
    private String accessToken;

    @Schema(description = "New JWT refresh token")
    private String refreshToken;

    @Schema(description = "Token type", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Access token expiration in seconds", example = "900")
    private Long expiresIn;

    @Schema(description = "Token issued at")
    private Long issuedAt;
}
