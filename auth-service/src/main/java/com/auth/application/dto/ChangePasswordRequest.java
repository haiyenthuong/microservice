package com.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Change Password Request DTO
 *
 * Chứa thông tin thay đổi mật khẩu
 *
 * @author Auth Service
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Change password request")
public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required")
    @Size(min = 6, max = 100, message = "Current password must be between 6 and 100 characters")
    @Schema(description = "Current password", example = "oldPassword123")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 6, max = 100, message = "New password must be between 6 and 100 characters")
    @Schema(description = "New password (min 6 characters)", example = "newPassword456")
    private String newPassword;

    @NotBlank(message = "Confirm password is required")
    @Size(min = 6, max = 100, message = "Confirm password must be between 6 and 100 characters")
    @Schema(description = "Confirm new password", example = "newPassword456")
    private String confirmPassword;
}
