package com.auth.application.command;

import com.auth.application.dto.*;
import com.auth.domain.model.User;
import com.auth.domain.repository.UserRepository;
import com.auth.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Command để xử lý login logic
 *
 * Flow:
 * 1. Validate username/password
 * 2. Check user status (active, locked, deleted)
 * 3. Generate access token và refresh token
 * 4. Return LoginResponse với tokens và user info
 *
 * @author Auth Service
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginCommand {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Execute login command
     *
     * @param request LoginRequest
     * @return LoginResponse với tokens và user info
     * @throws RuntimeException nếu login failed
     */
    @Transactional
    public LoginResponse execute(LoginRequest request) {
        log.info("Login attempt for username: {}", request.getUsername());

        // 1. Find user by username
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        // 2. Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Invalid password for username: {}", request.getUsername());
            throw new RuntimeException("Invalid username or password");
        }

        // 3. Check user status
        if (!user.isActive()) {
            if (user.isLocked()) {
                log.warn("Account locked for username: {}", request.getUsername());
                throw new RuntimeException("Account is locked. Please contact administrator.");
            }
            if (user.isDeleted()) {
                log.warn("Account deleted for username: {}", request.getUsername());
                throw new RuntimeException("Account has been deleted.");
            }
        }

        // 4. Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        log.info("Login successful for username: {}", request.getUsername());

        // 5. Build response
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration() / 1000)  // Convert to seconds
                .userId(user.getId())
                .username(user.getUsername())
                .fullname(user.getFullname())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .address(user.getAddress())
                .userType(user.getUserType())
                .userStatus(user.getUserStatus())
                .roles(jwtService.extractRoles(accessToken))
                .permissions(jwtService.extractPermissions(accessToken))
                .groups(jwtService.extractGroups(accessToken))
                .issuedAt(System.currentTimeMillis())
                .build();
    }
}
