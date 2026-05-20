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
public class UpdateParameterRequest {

    @Size(max = 2000, message = "Parameter value must not exceed 2000 characters")
    private String paramValue;

    @Size(max = 200, message = "Parameter name must not exceed 200 characters")
    private String paramName;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private Integer status;
}
