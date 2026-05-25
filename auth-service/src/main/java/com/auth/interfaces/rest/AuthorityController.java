package com.auth.interfaces.rest;

import com.auth.application.dto.AuthorityResponse;
import com.auth.application.dto.Response;
import com.auth.domain.model.Authority;
import com.auth.domain.repository.AuthorityRepository;
import com.auth.domain.repository.GroupAuthorityRepository;
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
 * REST Controller cho Authority Management APIs
 *
 * Endpoints:
 * - GET /authorities - Get all authorities
 * - GET /authorities/{id} - Get authority by ID
 * - GET /authorities/search - Search authorities
 * - GET /authorities/fid/{fid} - Get authorities by function ID
 * - POST /authorities - Create new authority
 * - PUT /authorities/{id} - Update authority
 * - DELETE /authorities/{id} - Delete authority
 *
 * @author Auth Service
 * @version 1.0.0
 */
@RestController
@RequestMapping("/authorities")
@RequiredArgsConstructor
@Tag(name = "Authority Management", description = "Authority/Permission management APIs")
public class AuthorityController {

    private final AuthorityRepository authorityRepository;
    private final GroupAuthorityRepository groupAuthorityRepository;

    /**
     * Get all authorities
     *
     * @return List of all authorities ordered by order_id
     */
    @GetMapping
    @Operation(summary = "Get all authorities", description = "Retrieve all authorities ordered by order_id")
    public Response<List<AuthorityResponse>> getAllAuthorities() {
        List<Authority> authorities = authorityRepository.findAllOrderByOrderId();
        List<AuthorityResponse> responses = authorities.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return Response.success("Authorities retrieved successfully", responses);
    }

    /**
     * Get authority by ID
     *
     * @param id Authority ID
     * @return Authority information
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get authority by ID", description = "Retrieve a specific authority by ID")
    public Response<AuthorityResponse> getAuthorityById(
            @PathVariable
            @Parameter(description = "Authority ID")
            String id) {
        Authority authority = authorityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Authority not found: " + id));
        return Response.success("Authority retrieved successfully", mapToResponse(authority));
    }

    /**
     * Search authorities by authority name or description
     *
     * @param keyword Search keyword
     * @return List of matching authorities
     */
    @GetMapping("/search")
    @Operation(summary = "Search authorities", description = "Search authorities by name or description")
    public Response<List<AuthorityResponse>> searchAuthorities(
            @RequestParam
            @Parameter(description = "Search keyword")
            String keyword) {
        List<Authority> authorities = authorityRepository.searchByAuthorityOrDescription(keyword);
        List<AuthorityResponse> responses = authorities.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return Response.success("Search completed", responses);
    }

    /**
     * Get authorities by function ID
     *
     * @param fid Function ID
     * @return List of authorities for the specified function
     */
    @GetMapping("/fid/{fid}")
    @Operation(summary = "Get authorities by function ID", description = "Get all authorities for a specific function")
    public Response<List<AuthorityResponse>> getAuthoritiesByFid(
            @PathVariable
            @Parameter(description = "Function ID")
            String fid) {
        List<Authority> authorities = authorityRepository.findByFid(fid);
        List<AuthorityResponse> responses = authorities.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return Response.success("Authorities retrieved successfully", responses);
    }

    /**
     * Create new authority
     *
     * @param request CreateAuthorityRequest
     * @return Created authority information
     */
    @PostMapping
    @Operation(summary = "Create authority", description = "Create a new authority/permission")
    public Response<AuthorityResponse> createAuthority(@Valid @RequestBody CreateAuthorityRequest request) {
        // Check if authority already exists
        if (authorityRepository.existsByAuthority(request.getAuthority())) {
            throw new RuntimeException("Authority already exists: " + request.getAuthority());
        }

        Authority authority = new Authority();
        authority.setAuthority(request.getAuthority());
        authority.setFid(request.getFid());
        authority.setDescription(request.getDescription());
        authority.setOrderId(request.getOrderId());
        authority.setAuthKey(request.getAuthKey());
        authority.setCreatedDate(LocalDateTime.now());
        authority.setUpdatedDate(LocalDateTime.now());

        Authority savedAuthority = authorityRepository.save(authority);

        return Response.success("Authority created successfully", mapToResponse(savedAuthority));
    }

    /**
     * Update authority
     *
     * @param id      Authority ID
     * @param request UpdateAuthorityRequest
     * @return Updated authority information
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update authority", description = "Update authority information")
    public Response<AuthorityResponse> updateAuthority(
            @PathVariable
            @Parameter(description = "Authority ID")
            String id,
            @Valid @RequestBody UpdateAuthorityRequest request) {

        Authority authority = authorityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Authority not found: " + id));

        // Update authority fields
        if (request.getDescription() != null) {
            authority.setDescription(request.getDescription());
        }
        if (request.getOrderId() != null) {
            authority.setOrderId(request.getOrderId());
        }
        if (request.getAuthKey() != null) {
            authority.setAuthKey(request.getAuthKey());
        }

        authority.setUpdatedDate(LocalDateTime.now());
        Authority updatedAuthority = authorityRepository.save(authority);

        return Response.success("Authority updated successfully", mapToResponse(updatedAuthority));
    }

    /**
     * Delete authority
     *
     * @param id Authority ID
     * @return Response
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete authority", description = "Delete authority")
    public Response<Void> deleteAuthority(
            @PathVariable
            @Parameter(description = "Authority ID")
            String id) {

        Authority authority = authorityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Authority not found: " + id));

        // Check if authority is being used by any groups
        long groupCount = groupAuthorityRepository.countByAuthority(authority);
        if (groupCount > 0) {
            throw new RuntimeException("Cannot delete authority: it is being used by " + groupCount + " group(s)");
        }

        authorityRepository.delete(authority);

        return Response.success("Authority deleted successfully", null);
    }

    /**
     * Map Authority entity to AuthorityResponse DTO
     *
     * @param authority Authority entity
     * @return AuthorityResponse DTO
     */
    private AuthorityResponse mapToResponse(Authority authority) {
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
     * Create Authority Request DTO
     */
    @Data
    public static class CreateAuthorityRequest {
        @NotBlank(message = "Authority is required")
        @Size(max = 100, message = "Authority must not exceed 100 characters")
        private String authority;

        @NotBlank(message = "Function ID is required")
        private String fid;

        @Size(max = 500, message = "Description must not exceed 500 characters")
        private String description;

        private Integer orderId = 0;

        @Size(max = 100, message = "Auth key must not exceed 100 characters")
        private String authKey;
    }

    /**
     * Update Authority Request DTO
     */
    @Data
    public static class UpdateAuthorityRequest {
        @Size(max = 500, message = "Description must not exceed 500 characters")
        private String description;

        private Integer orderId;

        @Size(max = 100, message = "Auth key must not exceed 100 characters")
        private String authKey;
    }
}
