package com.auth.interfaces.rest;

import com.auth.application.command.CreateUserCommand;
import com.auth.application.dto.*;
import com.auth.domain.model.User;
import com.auth.domain.repository.UserRepository;
import com.auth.domain.enums.UserStatus;
import com.auth.domain.enums.UserType;
import com.auth.infrastructure.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller cho User Management APIs
 *
 * Endpoints:
 * - GET /users - Get all users
 * - GET /users/{id} - Get user by ID
 * - GET /users/search - Search users
 * - GET /users/type/{type} - Get users by type
 * - POST /users - Create new user
 * - PUT /users/{id} - Update user
 * - PUT /users/{id}/lock - Lock user
 * - PUT /users/{id}/unlock - Unlock user
 * - DELETE /users/{id} - Delete user (soft delete)
 *
 * @author Auth Service
 * @version 1.0.0
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User management APIs")
public class UserController {

    private final UserRepository userRepository;
    private final CreateUserCommand createUserCommand;
    private final JwtService jwtService;

    /**
     * Get all users
     *
     * @return List of all users
     */
    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve all users")
    public Response<List<UserResponse>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponse> responses = users.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return Response.success("Users retrieved successfully", responses);
    }

    /**
     * Get user by ID
     *
     * @param id User ID
     * @return User information
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve a specific user by ID")
    public Response<UserResponse> getUserById(
            @PathVariable
            @Parameter(description = "User ID")
            String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        return Response.success("User retrieved successfully", mapToResponse(user));
    }

    /**
     * Search users by username or fullname
     *
     * @param keyword Search keyword
     * @return List of matching users
     */
    @GetMapping("/search")
    @Operation(summary = "Search users", description = "Search users by username or fullname")
    public Response<List<UserResponse>> searchUsers(
            @RequestParam
            @Parameter(description = "Search keyword")
            String keyword) {
        List<User> users = userRepository.searchByUsernameOrFullname(keyword);
        List<UserResponse> responses = users.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return Response.success("Search completed", responses);
    }

    /**
     * Get users by type
     *
     * @param type User type (0: ADMIN, 1: CUSTOMER)
     * @return List of users with specified type
     */
    @GetMapping("/type/{type}")
    @Operation(summary = "Get users by type", description = "Get users by type (0: ADMIN, 1: CUSTOMER)")
    public Response<List<UserResponse>> getUsersByType(
            @PathVariable
            @Parameter(description = "User type", example = "1")
            Integer type) {
        List<User> users = userRepository.findByType(type);
        List<UserResponse> responses = users.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return Response.success("Users retrieved successfully", responses);
    }

    /**
     * Create new user
     *
     * @param request CreateUserRequest
     * @return Created user information
     */
    @PostMapping
    @Operation(summary = "Create user", description = "Create a new user account")
    public Response<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = createUserCommand.execute(request);
        return Response.success("User created successfully", response);
    }

    /**
     * Update user
     *
     * @param id    User ID
     * @param request UpdateUserRequest
     * @return Updated user information
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update user information")
    public Response<UserResponse> updateUser(
            @PathVariable
            @Parameter(description = "User ID")
            String id,
            @Valid @RequestBody UpdateUserRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        // Update user fields
        if (request.getFullname() != null) {
            user.setFullname(request.getFullname());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getMobile() != null) {
            user.setMobile(request.getMobile());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }

        user.setUpdatedDate(LocalDateTime.now());
        User updatedUser = userRepository.save(user);

        return Response.success("User updated successfully", mapToResponse(updatedUser));
    }

    /**
     * Lock user
     *
     * @param id User ID
     * @return Response
     */
    @PutMapping("/{id}/lock")
    @Operation(summary = "Lock user", description = "Lock user account")
    public Response<Void> lockUser(
            @PathVariable
            @Parameter(description = "User ID")
            String id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        if (user.isLocked()) {
            throw new RuntimeException("User is already locked");
        }

        user.lock();
        userRepository.save(user);

        return Response.success("User locked successfully", null);
    }

    /**
     * Unlock user
     *
     * @param id User ID
     * @return Response
     */
    @PutMapping("/{id}/unlock")
    @Operation(summary = "Unlock user", description = "Unlock user account")
    public Response<Void> unlockUser(
            @PathVariable
            @Parameter(description = "User ID")
            String id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        if (!user.isLocked()) {
            throw new RuntimeException("User is not locked");
        }

        user.unlock();
        userRepository.save(user);

        return Response.success("User unlocked successfully", null);
    }

    /**
     * Delete user (soft delete)
     *
     * @param id User ID
     * @return Response
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Delete user account (soft delete)")
    public Response<Void> deleteUser(
            @PathVariable
            @Parameter(description = "User ID")
            String id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        if (user.isDeleted()) {
            throw new RuntimeException("User already deleted");
        }

        user.markAsDeleted();
        userRepository.save(user);

        // Revoke all tokens
        jwtService.revokeAllUserTokens(id);

        return Response.success("User deleted successfully", null);
    }

    /**
     * Map User entity to UserResponse DTO
     *
     * @param user User entity
     * @return UserResponse DTO
     */
    private UserResponse mapToResponse(User user) {
        // TODO: Implement role/permission/group extraction when entities are ready
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
