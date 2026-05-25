package com.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Update User Request DTO
 *
 * Chứa thông tin cập nhật user
 *
 * @author Auth Service
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update user request")
public class UpdateUserRequest {

    @Size(max = 100, message = "Full name must not exceed 100 characters")
    @Schema(description = "Full name", example = "John Doe")
    private String fullname;

    @Email(message = "Email must be valid")
    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;

    @Size(max = 10, message = "Mobile must not exceed 10 characters")
    @Schema(description = "Mobile number (10 digits)", example = "0901234567")
    private String mobile;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    @Schema(description = "Address", example = "123 Main St, City")
    private String address;

    @Schema(description = "List of group IDs to assign", example = "[\"group-id-1\", \"group-id-2\"]")
    private java.util.List<String> groupIds;
}
