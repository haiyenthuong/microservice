package com.auth.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Group Response DTO
 *
 * Used for returning group information in API responses
 *
 * @author Auth Service
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupResponse {

    /**
     * Group ID (UUID)
     */
    private String id;

    /**
     * Group name
     */
    private String groupName;

    /**
     * Group description
     */
    private String description;

    /**
     * Group type
     */
    private Integer type;

    /**
     * Group status (1: ACTIVE, 0: INACTIVE)
     */
    private Integer status;

    /**
     * Authority string
     */
    private String authority;

    /**
     * Created date
     */
    private LocalDateTime createdDate;

    /**
     * Updated date
     */
    private LocalDateTime updatedDate;
}
