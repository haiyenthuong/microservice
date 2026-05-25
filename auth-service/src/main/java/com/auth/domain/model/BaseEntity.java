package com.auth.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base Entity class cho tất cả entities
 *
 * Cung cấp các fields common:
 * - id: UUID primary key
 * - createdDate: Thời gian tạo
 * - updatedDate: Thời gian cập nhật
 * - createdBy: User tạo
 * - updatedBy: User cập nhật
 *
 * @author Auth Service
 * @version 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    /**
     * UUID primary key
     */
    @Id
    @Column(name = "id", length = 36)
    private String id;

    /**
     * Thời gian tạo record
     */
    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    /**
     * Thời gian cập nhật record
     */
    @LastModifiedDate
    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate;

    /**
     * User tạo record
     */
    @CreatedBy
    @Column(name = "created_by", length = 36)
    private String createdBy;

    /**
     * User cập nhật record
     */
    @LastModifiedBy
    @Column(name = "updated_by", length = 36)
    private String updatedBy;

    /**
     * Tạo UUID và thiết lập timestamps trước khi persist
     */
    @PrePersist
    protected void onCreate() {
        if (id == null || id.isEmpty()) {
            id = UUID.randomUUID().toString();
        }
        LocalDateTime now = LocalDateTime.now();
        createdDate = now;
        updatedDate = now;
    }

    /**
     * Cập nhật timestamp trước khi update
     */
    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
}
