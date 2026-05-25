package com.auth.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Entity representing Parameter (Tham số cấu hình) trong hệ thống
 *
 * Bảng: adm_parameter
 *
 * Chứa các tham số cấu hình:
 * - paramKey: Key của parameter (unique)
 * - paramValue: Value của parameter
 * - paramName: Tên hiển thị
 * - description: Mô tả
 * - status: Trạng thái
 *
 * @author Auth Service
 * @version 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "adm_parameter")
public class Parameter extends BaseEntity {

    /**
     * Key của parameter (unique)
     * Ví dụ: MAX_LOGIN_ATTEMPTS, SESSION_TIMEOUT, etc.
     */
    @NotBlank(message = "Parameter key is required")
    @Size(max = 100, message = "Parameter key must not exceed 100 characters")
    @Column(name = "param_key", length = 100, nullable = false, unique = true)
    private String paramKey;

    /**
     * Value của parameter
     */
    @NotBlank(message = "Parameter value is required")
    @Size(max = 2000, message = "Parameter value must not exceed 2000 characters")
    @Column(name = "param_value", length = 2000, nullable = false)
    private String paramValue;

    /**
     * Tên hiển thị của parameter
     */
    @NotBlank(message = "Parameter name is required")
    @Size(max = 200, message = "Parameter name must not exceed 200 characters")
    @Column(name = "param_name", length = 200, nullable = false)
    private String paramName;

    /**
     * Mô tả parameter
     */
    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Trạng thái parameter
     * 1: Active
     * 0: Inactive
     */
    @NotNull(message = "Status is required")
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    // Audit fields are inherited from BaseEntity
    // createdDate, updatedDate, createdBy, updatedBy

    /**
     * Kiểm tra parameter có active không
     *
     * @return true nếu active
     */
    public boolean isActive() {
        return status != null && status == 1;
    }

    /**
     * Activate parameter
     */
    public void activate() {
        this.status = 1;
        setUpdatedDate(LocalDateTime.now());
    }

    /**
     * Deactivate parameter
     */
    public void deactivate() {
        this.status = 0;
        setUpdatedDate(LocalDateTime.now());
    }
}
