package com.auth.application.command;

import com.auth.application.dto.CreateUserRequest;
import com.auth.application.dto.UserResponse;
import com.auth.domain.model.User;
import com.auth.domain.repository.UserRepository;
import com.auth.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Command để xử lý create user logic
 *
 * Flow:
 * 1. Validate request data
 * 2. Check uniqueness
 * 3. Encode password
 * 4. Create user entity
 * 5. Assign groups (nếu có)
 * 6. Save to database
 * 7. Return UserResponse
 *
 * @author Auth Service
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateUserCommand {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Execute create user command
     *
     * @param request CreateUserRequest
     * @return UserResponse
     * @throws RuntimeException nếu tạo user failed
     */
    @Transactional
    public UserResponse execute(CreateUserRequest request) {
        log.info("Create user attempt for username: {}", request.getUsername());

        // 1. Check username uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists: " + request.getUsername());
        }

        // 2. Check email uniqueness (nếu có email)
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (!userRepository.findByEmail(request.getEmail()).isEmpty()) {
                throw new RuntimeException("Email already exists: " + request.getEmail());
            }
        }

        // 3. Check mobile uniqueness (nếu có mobile)
        if (request.getMobile() != null && !request.getMobile().isEmpty()) {
            if (userRepository.existsByMobile(request.getMobile())) {
                throw new RuntimeException("Mobile already exists: " + request.getMobile());
            }
        }

        // 4. Encode password
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 5. Create user entity
        User user = User.builder()
                .username(request.getUsername())
                .password(encodedPassword)
                .fullname(request.getFullname())
                .email(request.getEmail())
                .mobile(request.getMobile())
                .address(request.getAddress())
                .type(request.getUserType().getValue())
                .build();

        // 6. Save to database
        User savedUser = userRepository.save(user);

        log.info("User created successfully: {}", savedUser.getUsername());

        // 7. TODO: Assign groups
        // if (request.getGroupIds() != null && !request.getGroupIds().isEmpty()) {
        //     assignUserToGroups(savedUser, request.getGroupIds());
        // }

        // 8. Build response
        return mapToResponse(savedUser);
    }

    /**
     * Map User entity sang UserResponse
     *
     * @param user User entity
     * @return UserResponse DTO
     */
    private UserResponse mapToResponse(User user) {
        // TODO: Extract roles, permissions, groups from relationships
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullname(user.getFullname())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .address(user.getAddress())
                .userType(user.getUserType())
                .userStatus(user.getUserStatus())
                // .roles(extractRoles(user))
                // .permissions(extractPermissions(user))
                // .groups(extractGroups(user))
                .createdDate(user.getCreatedDate())
                .updatedDate(user.getUpdatedDate())
                .createdBy(user.getCreatedBy())
                .updatedBy(user.getUpdatedBy())
                .build();
    }
}
