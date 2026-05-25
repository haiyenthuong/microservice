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
 * Entity representing Authority (Quyền hạn) trong hệ thống
 *
 * Bảng: adm_authorities
 *
 * Chứa thông tin quyền hạn:
 * - authority: Tên quyền hạn (unique) - VD: USER_READ, USER_WRITE
 * - fid: Functional ID - ID chức năng
 * - description: Mô tả quyền hạn
 * - orderId: Thứ tự sắp xếp
 * - authKey: Key để xác định quyền trong code
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
@Table(name = "adm_authorities")
public class Authority extends BaseEntity {

    /**
     * Tên quyền hạn (unique)
     * Ví dụ: USER_READ, USER_WRITE, USER_DELETE, ORDER_MANAGE
     */
    @NotBlank(message = "Authority is required")
    @Size(max = 200, message = "Authority must not exceed 200 characters")
    @Column(name = "authority", length = 200, nullable = false, unique = true)
    private String authority;

    /**
     * Functional ID - ID chức năng
     */
    @NotBlank(message = "FID is required")
    @Size(max = 50, message = "FID must not exceed 50 characters")
    @Column(name = "fid", length = 50, nullable = false)
    private String fid;

    /**
     * Mô tả quyền hạn
     */
    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", length = 500, nullable = false)
    private String description;

    /**
     * Thứ tự hiển thị
     */
    @NotNull(message = "Order ID is required")
    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    /**
     * Key để dùng trong code (VD: annotation @PreAuthorize)
     */
    @Size(max = 100, message = "Auth key must not exceed 100 characters")
    @Column(name = "auth_key", length = 100)
    private String authKey;

    // Audit fields are inherited from BaseEntity
    // createdDate, updatedDate, createdBy, updatedBy
}
