package com.auth.application.query.user;

import com.auth.application.dto.UserResponse;
import com.auth.domain.model.User;
import com.auth.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Query handler for retrieving a user by ID
 *
 * @author Auth Service
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class GetUserByIdQuery {

    private final UserRepository userRepository;

    /**
     * Execute query to get user by ID
     *
     * @param userId User ID
     * @return UserResponse
     */
    @Transactional(readOnly = true)
    public UserResponse execute(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        return mapToResponse(user);
    }

    /**
     * Map User entity to UserResponse DTO
     */
    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullname(user.getFullname())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .address(user.getAddress())
                .userType(user.getUserType())
                .userStatus(user.getUserStatus())
                .roles(List.of()) // TODO: Extract from groups
                .permissions(List.of()) // TODO: Extract from authorities
                .groups(List.of()) // TODO: Extract from group_users
                .createdDate(user.getCreatedDate())
                .updatedDate(user.getUpdatedDate())
                .createdBy(user.getCreatedBy())
                .updatedBy(user.getUpdatedBy())
                .build();
    }
}
