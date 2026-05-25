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
 * Entity representing Group trong hệ thống
 *
 * Bảng: adm_group
 *
 * Chứa thông tin group:
 * - groupName: Tên group
 * - status: Trạng thái group
 * - authority: Mô tả quyền hạn của group
 * - description: Mô tả group
 * - type: Loại group
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
@Table(name = "adm_group")
public class Group extends BaseEntity {

    /**
     * Tên group
     */
    @NotBlank(message = "Group name is required")
    @Size(max = 200, message = "Group name must not exceed 200 characters")
    @Column(name = "group_name", length = 200, nullable = false)
    private String groupName;

    /**
     * Trạng thái group
     * 1: Active
     * 0: Inactive
     */
    @NotNull(message = "Status is required")
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    /**
     * Mô tả quyền hạn của group (text field, backup)
     */
    @Size(max = 500, message = "Authority must not exceed 500 characters")
    @Column(name = "authority", length = 500)
    private String authority;

    /**
     * Mô tả group
     */
    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Loại group
     */
    @Column(name = "type")
    private Integer type;

    // Audit fields are inherited from BaseEntity
    // createdDate, updatedDate, createdBy, updatedBy
}
