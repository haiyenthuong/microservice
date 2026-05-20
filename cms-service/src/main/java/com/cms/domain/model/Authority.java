package com.cms.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "adm_authorities")
public class Authority extends BaseEntity {

    @Column(name = "authority", length = 200, nullable = false, unique = true)
    private String authority;

    @Column(name = "fid", length = 50, nullable = false)
    private String fid;

    @Column(name = "description", length = 500, nullable = false)
    private String description;

    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @Column(name = "auth_key", length = 100)
    private String authKey;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}
