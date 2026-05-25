package com.auth.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Parameter Response DTO
 *
 * Used for returning parameter information in API responses
 *
 * @author Auth Service
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParameterResponse {

    /**
     * Parameter ID (UUID)
     */
    private String id;

    /**
     * Parameter key (unique identifier)
     */
    private String paramKey;

    /**
     * Parameter value
     */
    private String paramValue;

    /**
     * Parameter display name
     */
    private String paramName;

    /**
     * Parameter description
     */
    private String description;

    /**
     * Parameter status (1: ACTIVE, 0: INACTIVE)
     */
    private Integer status;

    /**
     * Created date
     */
    private LocalDateTime createdDate;

    /**
     * Updated date
     */
    private LocalDateTime updatedDate;
}
