package com.cms.application.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAuthorityRequest {

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private Integer orderId;

    @Size(max = 100, message = "Auth key must not exceed 100 characters")
    private String authKey;
}
