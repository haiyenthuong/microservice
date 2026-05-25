package com.auth.interfaces.rest;

import com.auth.application.dto.*;
import com.auth.domain.model.Authority;
import com.auth.domain.model.Group;
import com.auth.domain.model.GroupAuthority;
import com.auth.domain.model.GroupUser;
import com.auth.domain.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller cho Group Management APIs
 *
 * Endpoints:
 * - GET /groups - Get all groups
 * - GET /groups/{id} - Get group by ID
 * - GET /groups/search - Search groups
 * - GET /groups/type/{type} - Get groups by type
 * - POST /groups - Create new group
 * - PUT /groups/{id} - Update group
 * - DELETE /groups/{id} - Delete group
 * - POST /groups/{id}/authorities - Add authority to group
 * - DELETE /groups/{id}/authorities/{authorityId} - Remove authority from group
 * - GET /groups/{id}/authorities - Get all authorities of group
 * - POST /groups/{id}/users - Add user to group
 * - DELETE /groups/{id}/users/{userId} - Remove user from group
 * - GET /groups/{id}/users - Get all users of group
 *
 * @author Auth Service
 * @version 1.0.0
 */
@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
@Tag(name = "Group Management", description = "Group management APIs")
public class GroupController {

    private final GroupRepository groupRepository;
    private final AuthorityRepository authorityRepository;
    private final UserRepository userRepository;
    private final GroupAuthorityRepository groupAuthorityRepository;
    private final GroupUserRepository groupUserRepository;

    /**
     * Get all groups
     *
     * @return List of all groups
     */
    @GetMapping
    @Operation(summary = "Get all groups", description = "Retrieve all groups")
    public Response<List<GroupResponse>> getAllGroups() {
        List<Group> groups = groupRepository.findAll();
        List<GroupResponse> responses = groups.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return Response.success("Groups retrieved successfully", responses);
    }

    /**
     * Get group by ID
     *
     * @param id Group ID
     * @return Group information
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get group by ID", description = "Retrieve a specific group by ID")
    public Response<GroupResponse> getGroupById(
            @PathVariable
            @Parameter(description = "Group ID")
            String id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found: " + id));
        return Response.success("Group retrieved successfully", mapToResponse(group));
    }

    /**
     * Search groups by group name or description
     *
     * @param keyword Search keyword
     * @return List of matching groups
     */
    @GetMapping("/search")
    @Operation(summary = "Search groups", description = "Search groups by name or description")
    public Response<List<GroupResponse>> searchGroups(
            @RequestParam
            @Parameter(description = "Search keyword")
            String keyword) {
        List<Group> groups = groupRepository.searchByGroupNameOrDescription(keyword);
        List<GroupResponse> responses = groups.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return Response.success("Search completed", responses);
    }

    /**
     * Get groups by type
     *
     * @param type Group type
     * @return List of groups with specified type
     */
    @GetMapping("/type/{type}")
    @Operation(summary = "Get groups by type", description = "Get groups by type")
    public Response<List<GroupResponse>> getGroupsByType(
            @PathVariable
            @Parameter(description = "Group type")
            Integer type) {
        List<Group> groups = groupRepository.findByType(type);
        List<GroupResponse> responses = groups.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return Response.success("Groups retrieved successfully", responses);
    }

    /**
     * Create new group
     *
     * @param request CreateGroupRequest
     * @return Created group information
     */
    @PostMapping
    @Operation(summary = "Create group", description = "Create a new group")
    public Response<GroupResponse> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        // Check if group name already exists
        if (groupRepository.existsByGroupName(request.getGroupName())) {
            throw new RuntimeException("Group name already exists: " + request.getGroupName());
        }

        Group group = new Group();
        group.setGroupName(request.getGroupName());
        group.setDescription(request.getDescription());
        group.setType(request.getType());
        group.setStatus(1); // Default ACTIVE
        group.setAuthority(request.getAuthority());
        group.setCreatedDate(LocalDateTime.now());
        group.setUpdatedDate(LocalDateTime.now());

        Group savedGroup = groupRepository.save(group);

        return Response.success("Group created successfully", mapToResponse(savedGroup));
    }

    /**
     * Update group
     *
     * @param id      Group ID
     * @param request UpdateGroupRequest
     * @return Updated group information
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update group", description = "Update group information")
    public Response<GroupResponse> updateGroup(
            @PathVariable
            @Parameter(description = "Group ID")
            String id,
            @Valid @RequestBody UpdateGroupRequest request) {

        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found: " + id));

        // Update group fields
        if (request.getDescription() != null) {
            group.setDescription(request.getDescription());
        }
        if (request.getType() != null) {
            group.setType(request.getType());
        }
        if (request.getAuthority() != null) {
            group.setAuthority(request.getAuthority());
        }
        if (request.getStatus() != null) {
            group.setStatus(request.getStatus());
        }

        group.setUpdatedDate(LocalDateTime.now());
        Group updatedGroup = groupRepository.save(group);

        return Response.success("Group updated successfully", mapToResponse(updatedGroup));
    }

    /**
     * Delete group
     *
     * @param id Group ID
     * @return Response
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete group", description = "Delete group")
    public Response<Void> deleteGroup(
            @PathVariable
            @Parameter(description = "Group ID")
            String id) {

        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found: " + id));

        // Remove all group-authority relationships
        List<GroupAuthority> groupAuthorities = groupAuthorityRepository.findByGroup(group);
        groupAuthorityRepository.deleteAll(groupAuthorities);

        // Remove all group-user relationships
        List<GroupUser> groupUsers = groupUserRepository.findByGroup(group);
        groupUserRepository.deleteAll(groupUsers);

        // Delete group
        groupRepository.delete(group);

        return Response.success("Group deleted successfully", null);
    }

    /**
     * Add authority to group
     *
     * @param id          Group ID
     * @param authorityId Authority ID
     * @return Response
     */
    @PostMapping("/{id}/authorities")
    @Operation(summary = "Add authority to group", description = "Add an authority to a group")
    public Response<Void> addAuthorityToGroup(
            @PathVariable
            @Parameter(description = "Group ID")
            String id,
            @RequestParam
            @Parameter(description = "Authority ID")
            String authorityId) {

        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found: " + id));

        Authority authority = authorityRepository.findById(authorityId)
                .orElseThrow(() -> new RuntimeException("Authority not found: " + authorityId));

        // Check if relationship already exists
        if (groupAuthorityRepository.existsByGroupAndAuthority(group, authority)) {
            throw new RuntimeException("Authority already added to group");
        }

        GroupAuthority groupAuthority = new GroupAuthority();
        groupAuthority.setGroup(group);
        groupAuthority.setAuthority(authority);
        groupAuthority.setCreatedDate(LocalDateTime.now());

        groupAuthorityRepository.save(groupAuthority);

        return Response.success("Authority added to group successfully", null);
    }

    /**
     * Remove authority from group
     *
     * @param id          Group ID
     * @param authorityId Authority ID
     * @return Response
     */
    @DeleteMapping("/{id}/authorities/{authorityId}")
    @Operation(summary = "Remove authority from group", description = "Remove an authority from a group")
    public Response<Void> removeAuthorityFromGroup(
            @PathVariable
            @Parameter(description = "Group ID")
            String id,
            @PathVariable
            @Parameter(description = "Authority ID")
            String authorityId) {

        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found: " + id));

        Authority authority = authorityRepository.findById(authorityId)
                .orElseThrow(() -> new RuntimeException("Authority not found: " + authorityId));

        GroupAuthority groupAuthority = groupAuthorityRepository.findByGroupAndAuthority(group, authority)
                .orElseThrow(() -> new RuntimeException("Authority not found in group"));

        groupAuthorityRepository.delete(groupAuthority);

        return Response.success("Authority removed from group successfully", null);
    }

    /**
     * Get all authorities of group
     *
     * @param id Group ID
     * @return List of authorities
     */
    @GetMapping("/{id}/authorities")
    @Operation(summary = "Get group authorities", description = "Get all authorities of a group")
    public Response<List<AuthorityResponse>> getGroupAuthorities(
            @PathVariable
            @Parameter(description = "Group ID")
            String id) {

        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found: " + id));

        List<GroupAuthority> groupAuthorities = groupAuthorityRepository.findByGroup(group);
        List<AuthorityResponse> responses = groupAuthorities.stream()
                .map(ga -> mapToAuthorityResponse(ga.getAuthority()))
                .collect(Collectors.toList());

        return Response.success("Group authorities retrieved successfully", responses);
    }

    /**
     * Add user to group
     *
     * @param id     Group ID
     * @param userId User ID
     * @return Response
     */
    @PostMapping("/{id}/users")
    @Operation(summary = "Add user to group", description = "Add a user to a group")
    public Response<Void> addUserToGroup(
            @PathVariable
            @Parameter(description = "Group ID")
            String id,
            @RequestParam
            @Parameter(description = "User ID")
            String userId) {

        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found: " + id));

        com.auth.domain.model.User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Check if relationship already exists
        if (groupUserRepository.existsByUserAndGroup(user, group)) {
            throw new RuntimeException("User already added to group");
        }

        GroupUser groupUser = new GroupUser();
        groupUser.setGroup(group);
        groupUser.setUser(user);
        groupUser.setCreatedDate(LocalDateTime.now());

        groupUserRepository.save(groupUser);

        return Response.success("User added to group successfully", null);
    }

    /**
     * Remove user from group
     *
     * @param id     Group ID
     * @param userId User ID
     * @return Response
     */
    @DeleteMapping("/{id}/users/{userId}")
    @Operation(summary = "Remove user from group", description = "Remove a user from a group")
    public Response<Void> removeUserFromGroup(
            @PathVariable
            @Parameter(description = "Group ID")
            String id,
            @PathVariable
            @Parameter(description = "User ID")
            String userId) {

        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found: " + id));

        com.auth.domain.model.User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        GroupUser groupUser = groupUserRepository.findByUserAndGroup(user, group)
                .orElseThrow(() -> new RuntimeException("User not found in group"));

        groupUserRepository.delete(groupUser);

        return Response.success("User removed from group successfully", null);
    }

    /**
     * Get all users of group
     *
     * @param id Group ID
     * @return List of users
     */
    @GetMapping("/{id}/users")
    @Operation(summary = "Get group users", description = "Get all users of a group")
    public Response<List<UserResponse>> getGroupUsers(
            @PathVariable
            @Parameter(description = "Group ID")
            String id) {

        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found: " + id));

        List<GroupUser> groupUsers = groupUserRepository.findByGroup(group);
        List<UserResponse> responses = groupUsers.stream()
                .map(gu -> mapToUserResponse(gu.getUser()))
                .collect(Collectors.toList());

        return Response.success("Group users retrieved successfully", responses);
    }

    /**
     * Map Group entity to GroupResponse DTO
     */
    private GroupResponse mapToResponse(Group group) {
        return GroupResponse.builder()
                .id(group.getId())
                .groupName(group.getGroupName())
                .description(group.getDescription())
                .type(group.getType())
                .status(group.getStatus())
                .authority(group.getAuthority())
                .createdDate(group.getCreatedDate())
                .updatedDate(group.getUpdatedDate())
                .build();
    }

    /**
     * Map Authority entity to AuthorityResponse DTO
     */
    private AuthorityResponse mapToAuthorityResponse(Authority authority) {
        return AuthorityResponse.builder()
                .id(authority.getId())
                .authority(authority.getAuthority())
                .fid(authority.getFid())
                .description(authority.getDescription())
                .orderId(authority.getOrderId())
                .authKey(authority.getAuthKey())
                .createdDate(authority.getCreatedDate())
                .updatedDate(authority.getUpdatedDate())
                .build();
    }

    /**
     * Map User entity to UserResponse DTO
     */
    private UserResponse mapToUserResponse(com.auth.domain.model.User user) {
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

    /**
     * Create Group Request DTO
     */
    @Data
    public static class CreateGroupRequest {
        @NotBlank(message = "Group name is required")
        @Size(max = 100, message = "Group name must not exceed 100 characters")
        private String groupName;

        @Size(max = 500, message = "Description must not exceed 500 characters")
        private String description;

        @NotNull(message = "Type is required")
        private Integer type;

        @Size(max = 100, message = "Authority must not exceed 100 characters")
        private String authority;
    }

    /**
     * Update Group Request DTO
     */
    @Data
    public static class UpdateGroupRequest {
        @Size(max = 500, message = "Description must not exceed 500 characters")
        private String description;

        private Integer type;

        @Size(max = 100, message = "Authority must not exceed 100 characters")
        private String authority;

        private Integer status;
    }
}
