package com.auth.application.command;

import com.auth.application.dto.RefreshTokenRequest;
import com.auth.application.dto.RefreshTokenResponse;
import com.auth.domain.model.User;
import com.auth.domain.repository.UserRepository;
import com.auth.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Command để xử lý refresh token logic
 *
 * Flow:
 * 1. Validate refresh token
 * 2. Extract user ID from token
 * 3. Get user from database
 * 4. Check user status (still active, not locked/deleted)
 * 5. Generate new access token
 * 6. (Optional) Generate new refresh token
 * 7. Return RefreshTokenResponse
 *
 * @author Auth Service
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCommand {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    /**
     * Execute refresh token command
     *
     * @param request RefreshTokenRequest
     * @return RefreshTokenResponse với new tokens
     * @throws RuntimeException nếu refresh failed
     */
    @Transactional
    public RefreshTokenResponse execute(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        log.info("Refresh token attempt");

        // 1. Validate refresh token và extract user ID
        String userId = jwtService.validateRefreshToken(refreshToken);
        if (userId == null) {
            log.warn("Invalid or expired refresh token");
            throw new RuntimeException("Invalid or expired refresh token");
        }

        // 2. Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Check user status
        if (!user.isActive()) {
            if (user.isLocked()) {
                log.warn("Account locked for user: {}", userId);
                throw new RuntimeException("Account is locked");
            }
            if (user.isDeleted()) {
                log.warn("Account deleted for user: {}", userId);
                throw new RuntimeException("Account has been deleted");
            }
        }

        // 4. Generate new access token
        String newAccessToken = jwtService.generateAccessToken(user);

        // 5. Generate new refresh token (rotate refresh token for security)
        String newRefreshToken = jwtService.generateRefreshToken(user);

        log.info("Token refreshed successfully for user: {}", userId);

        // 6. Build response
        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration() / 1000)
                .issuedAt(System.currentTimeMillis())
                .build();
    }
}
