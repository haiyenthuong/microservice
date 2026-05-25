package com.auth.application.command;

import com.auth.application.dto.ChangePasswordRequest;
import com.auth.domain.model.User;
import com.auth.domain.repository.UserRepository;
import com.auth.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Command để xử lý change password logic
 *
 * Flow:
 * 1. Validate request data
 * 2. Get current user from context
 * 3. Validate current password
 * 4. Encode new password
 * 5. Update user password
 * 6. Revoke all existing refresh tokens (optional, for security)
 *
 * @author Auth Service
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChangePasswordCommand {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Execute change password command
     *
     * @param userId    ID của user cần đổi password
     * @param request   ChangePasswordRequest
     * @throws RuntimeException nếu change password failed
     */
    @Transactional
    public void execute(String userId, ChangePasswordRequest request) {
        log.info("Change password attempt for user: {}", userId);

        // 1. Validate password confirmation
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New password and confirm password do not match");
        }

        if (request.getNewPassword().equals(request.getCurrentPassword())) {
            throw new RuntimeException("New password must be different from current password");
        }

        // 2. Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Validate current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.warn("Invalid current password for user: {}", userId);
            throw new RuntimeException("Current password is incorrect");
        }

        // 4. Encode new password
        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());

        // 5. Update password
        user.updatePassword(encodedNewPassword);
        userRepository.save(user);

        // 6. Revoke all refresh tokens (force login again on all devices)
        jwtService.revokeAllUserTokens(userId);

        log.info("Password changed successfully for user: {}", userId);
    }
}
