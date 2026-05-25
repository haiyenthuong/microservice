package com.auth.interfaces.rest;

import com.auth.application.command.*;
import com.auth.application.dto.*;
import com.auth.infrastructure.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller cho Authentication APIs
 *
 * Endpoints:
 * - POST /auth/login - Login
 * - POST /auth/register - Register
 * - POST /auth/refresh-token - Refresh access token
 * - POST /auth/change-password - Change password
 * - POST /auth/logout - Logout (revoke refresh token)
 * - GET  /auth/me - Get current user info
 *
 * @author Auth Service
 * @version 1.0.0
 */
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and Authorization APIs")
public class AuthController {

    private final LoginCommand loginCommand;
    private final RegisterCommand registerCommand;
    private final ChangePasswordCommand changePasswordCommand;
    private final RefreshTokenCommand refreshTokenCommand;
    private final JwtService jwtService;

    /**
     * Login endpoint
     *
     * Validate username/password và return JWT tokens
     *
     * @param request LoginRequest
     * @return Response with LoginResponse
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT tokens")
    public Response<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = loginCommand.execute(request);
        return Response.success("Login successful", response);
    }

    /**
     * Register endpoint
     *
     * Create new user account và return JWT tokens
     *
     * @param request RegisterRequest
     * @return Response with LoginResponse
     */
    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Create new user account")
    public Response<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = registerCommand.execute(request);
        return Response.success("Registration successful", response);
    }

    /**
     * Refresh token endpoint
     *
     * Get new access token bằng refresh token
     *
     * @param request RefreshTokenRequest
     * @return Response with RefreshTokenResponse
     */
    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token", description = "Get new access token using refresh token")
    public Response<RefreshTokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse response = refreshTokenCommand.execute(request);
        return Response.success("Token refreshed successfully", response);
    }

    /**
     * Change password endpoint
     *
     * Thay đổi mật khẩu user hiện tại
     *
     * @param request ChangePasswordRequest
     * @return Response
     */
    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Change current user password")
    public Response<Void> changePassword(
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        changePasswordCommand.execute(userId, request);
        return Response.success("Password changed successfully", null);
    }

    /**
     * Logout endpoint
     *
     * Revoke refresh token để logout
     *
     * @param userId User ID from header
     * @return Response
     */
    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Revoke refresh token to logout")
    public Response<Void> logout(
            @RequestHeader(value = "X-User-Id", required = true) String userId) {
        jwtService.revokeAllUserTokens(userId);
        return Response.success("Logged out successfully", null);
    }

    /**
     * Get current user info endpoint
     *
     * Lấy thông tin user đang login
     *
     * @param userId User ID from header
     * @return Response with UserResponse
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get current logged in user information")
    public Response<UserResponse> getCurrentUser(
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestHeader(value = "X-User-Name", required = true) String username) {

        // TODO: Implement GetUserByIdQuery
        // For now, return basic info
        UserResponse response = UserResponse.builder()
                .id(userId)
                .username(username)
                .build();

        return Response.success("User retrieved successfully", response);
    }
}
