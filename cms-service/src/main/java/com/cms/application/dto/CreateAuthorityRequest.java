package com.cms.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAuthorityRequest {

    @NotBlank(message = "authority is required")
    @Size(max = 200, message = "authority must not exceed 200 characters")
    private String authority;

    @Size(max = 36, message = "fid must not exceed 36 characters")
    private String fid;

    @NotBlank(message = "description is required")
    @Size(max = 500, message = "description must not exceed 500 characters")
    private String description;

    @NotNull(message = "orderId is required")
    private Integer orderId;

    @NotBlank(message = "authKey is required")
    @Size(max = 100, message = "Auth key must not exceed 100 characters")
    private String authKey;
}
