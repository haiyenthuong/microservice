package com.auth.application.dto;

import com.auth.domain.enums.UserStatus;
import com.auth.domain.enums.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * User Response DTO
 *
 * Chứa thông tin user trả về cho client
 *
 * @author Auth Service
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User response")
public class UserResponse {

    @Schema(description = "User ID")
    private String id;

    @Schema(description = "Username")
    private String username;

    @Schema(description = "Full name")
    private String fullname;

    @Schema(description = "Email")
    private String email;

    @Schema(description = "Mobile number")
    private String mobile;

    @Schema(description = "Address")
    private String address;

    @Schema(description = "User type", example = "CUSTOMER")
    private UserType userType;

    @Schema(description = "User status", example = "ACTIVE")
    private UserStatus userStatus;

    @Schema(description = "User roles")
    private List<String> roles;

    @Schema(description = "User permissions")
    private List<String> permissions;

    @Schema(description = "User groups")
    private List<String> groups;

    @Schema(description = "Created date")
    private LocalDateTime createdDate;

    @Schema(description = "Updated date")
    private LocalDateTime updatedDate;

    @Schema(description = "Created by")
    private String createdBy;

    @Schema(description = "Updated by")
    private String updatedBy;
}
