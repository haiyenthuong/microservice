package com.payment.domain.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base Entity class cho tất cả entities trong Payment Service.
 * Chứa các common fields và auditing information.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    /**
     * Primary Key - UUID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    /**
     * Thời gian tạo bản ghi
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    /**
     * Thời gian cập nhật bản ghi
     */
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    /**
     * Người tạo bản ghi (user ID hoặc system)
     */
    @Column(name = "created_by", length = 100)
    @CreatedBy
    public String createdBy;

    /**
     * Người cập nhật bản ghi gần nhất
     */
    @Column(name = "updated_by", length = 100)
    @LastModifiedBy
    public String updatedBy;

    /**
     * Bản ghi có bị xóa không (soft delete)
     */
    @Column(name = "is_deleted", nullable = false)
    public Boolean isDeleted = false;

    /**
     * Thời gian xóa bản ghi (soft delete)
     */
    @Column(name = "deleted_at")
    public LocalDateTime deletedAt;

    /**
     * Version number để optimistic locking
     */
    @Version
    @Column(name = "version", nullable = false)
    public Long version = 0L;

    /**
     * Callback method được gọi BEFORE persist.
     * Tự động set createdAt và updatedAt.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Callback method được gọi BEFORE update.
     * Tự động update updatedAt.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
