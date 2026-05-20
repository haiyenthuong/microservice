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
@Table(name = "adm_group")
public class Group extends BaseEntity {

    @Column(name = "group_name", length = 200, nullable = false)
    private String groupName;

    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "authority", length = 500)
    private String authority;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "type")
    private Integer type;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "udpated_by", length = 50)  // Note: typo in original schema
    private String updatedBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}
