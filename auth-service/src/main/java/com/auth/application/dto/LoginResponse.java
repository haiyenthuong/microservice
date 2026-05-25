package com.auth.application.dto;

import com.auth.domain.enums.UserStatus;
import com.auth.domain.enums.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Login Response DTO
 *
 * Chứa thông tin sau khi đăng nhập thành công
 *
 * @author Auth Service
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login response")
public class LoginResponse {

    @Schema(description = "JWT access token")
    private String accessToken;

    @Schema(description = "JWT refresh token")
    private String refreshToken;

    @Schema(description = "Token type", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Access token expiration in seconds", example = "900")
    private Long expiresIn;

    @Schema(description = "User ID")
    private String userId;

    @Schema(description = "Username")
    private String username;

    @Schema(description = "Full name")
    private String fullname;

    @Schema(description = "Email")
    private String email;

    @Schema(description = "Mobile")
    private String mobile;

    @Schema(description = "Address")
    private String address;

    @Schema(description = "User type", example = "CUSTOMER")
    private UserType userType;

    @Schema(description = "User status", example = "ACTIVE")
    private UserStatus userStatus;

    @Schema(description = "User roles")
    private List<String> roles;

    @Schema(description = "User permissions")
    private List<String> permissions;

    @Schema(description = "User groups")
    private List<String> groups;

    @Schema(description = "Token issued at (timestamp)")
    private Long issuedAt;

    @Schema(description = "Token expires at (timestamp)")
    private Long expiresAt;
}
