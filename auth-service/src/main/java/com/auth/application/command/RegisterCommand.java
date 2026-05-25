package com.auth.application.command;

import com.auth.application.dto.*;
import com.auth.domain.enums.UserStatus;
import com.auth.domain.enums.UserType;
import com.auth.domain.model.User;
import com.auth.domain.repository.UserRepository;
import com.auth.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Command để xử lý registration logic
 *
 * Flow:
 * 1. Validate request data
 * 2. Check username/email/mobile uniqueness
 * 3. Encode password
 * 4. Create user entity
 * 5. Save to database
 * 6. Generate tokens
 * 7. Return LoginResponse
 *
 * @author Auth Service
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterCommand {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Execute registration command
     *
     * @param request RegisterRequest
     * @return LoginResponse với tokens và user info
     * @throws RuntimeException nếu registration failed
     */
    @Transactional
    public LoginResponse execute(RegisterRequest request) {
        log.info("Registration attempt for username: {}", request.getUsername());

        // 1. Validate password confirmation
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Password and confirm password do not match");
        }

        // 2. Check username uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Username already exists: {}", request.getUsername());
            throw new RuntimeException("Username already exists");
        }

        // 3. Check email uniqueness (nếu có email)
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (!userRepository.findByEmail(request.getEmail()).isEmpty()) {
                log.warn("Email already exists: {}", request.getEmail());
                throw new RuntimeException("Email already exists");
            }
        }

        // 4. Check mobile uniqueness (nếu có mobile)
        if (request.getMobile() != null && !request.getMobile().isEmpty()) {
            if (userRepository.existsByMobile(request.getMobile())) {
                log.warn("Mobile already exists: {}", request.getMobile());
                throw new RuntimeException("Mobile number already exists");
            }
        }

        // 5. Encode password
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 6. Create user entity
        User user = User.builder()
                .username(request.getUsername())
                .password(encodedPassword)
                .fullname(request.getFullname())
                .email(request.getEmail())
                .mobile(request.getMobile())
                .address(request.getAddress())
                .type(request.getUserType() != null ?
                    request.getUserType().getValue() : UserType.CUSTOMER.getValue())
                .status(UserStatus.ACTIVE.getValue())
                .build();

        // 7. Save to database
        User savedUser = userRepository.save(user);

        log.info("User registered successfully: {}", savedUser.getUsername());

        // 8. Generate tokens (auto-login after registration)
        String accessToken = jwtService.generateAccessToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);

        // 9. Build response
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration() / 1000)
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .fullname(savedUser.getFullname())
                .email(savedUser.getEmail())
                .mobile(savedUser.getMobile())
                .address(savedUser.getAddress())
                .userType(savedUser.getUserType())
                .userStatus(savedUser.getUserStatus())
                .roles(jwtService.extractRoles(accessToken))
                .permissions(jwtService.extractPermissions(accessToken))
                .groups(jwtService.extractGroups(accessToken))
                .issuedAt(System.currentTimeMillis())
                .build();
    }
}
