package com.auth.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Authority Response DTO
 *
 * Used for returning authority information in API responses
 *
 * @author Auth Service
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorityResponse {

    /**
     * Authority ID (UUID)
     */
    private String id;

    /**
     * Authority code (e.g., "USER_READ", "USER_WRITE")
     */
    private String authority;

    /**
     * Function ID that this authority belongs to
     */
    private String fid;

    /**
     * Authority description
     */
    private String description;

    /**
     * Display order
     */
    private Integer orderId;

    /**
     * Authorization key
     */
    private String authKey;

    /**
     * Created date
     */
    private LocalDateTime createdDate;

    /**
     * Updated date
     */
    private LocalDateTime updatedDate;
}
