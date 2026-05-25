package com.auth.application.dto;

import com.auth.domain.enums.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Register Request DTO
 *
 * Chứa thông tin đăng ký user mới
 *
 * @author Auth Service
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Registration request")
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username must contain only alphanumeric characters and underscore")
    @Schema(description = "Username (unique)", example = "john_doe")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    @Schema(description = "Password (min 6 characters)", example = "password123")
    private String password;

    @NotBlank(message = "Confirm password is required")
    @Schema(description = "Confirm password", example = "password123")
    private String confirmPassword;

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    @Schema(description = "Full name", example = "John Doe")
    private String fullname;

    @Email(message = "Email must be valid")
    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;

    @Size(max = 10, message = "Mobile must not exceed 10 characters")
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile must be 10 digits")
    @Schema(description = "Mobile number (10 digits)", example = "0901234567")
    private String mobile;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    @Schema(description = "Address", example = "123 Main St, City")
    private String address;

    @Schema(description = "User type (optional, defaults to CUSTOMER)", example = "CUSTOMER")
    private UserType userType;
}
