package com.auth.application.query.user;

import com.auth.application.dto.UserResponse;
import com.auth.domain.model.User;
import com.auth.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Query handler for retrieving all users
 *
 * @author Auth Service
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class GetAllUsersQuery {

    private final UserRepository userRepository;

    /**
     * Execute query to get all users
     *
     * @return List of UserResponse
     */
    @Transactional(readOnly = true)
    public List<UserResponse> execute() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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
                .roles(List.of())
                .permissions(List.of())
                .groups(List.of())
                .createdDate(user.getCreatedDate())
                .updatedDate(user.getUpdatedDate())
                .createdBy(user.getCreatedBy())
                .updatedBy(user.getUpdatedBy())
                .build();
    }
}
