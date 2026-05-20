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
@Table(name = "adm_parameter")
public class Parameter extends BaseEntity {

    @Column(name = "param_key", length = 100, nullable = false, unique = true)
    private String paramKey;

    @Column(name = "param_value", length = 2000, nullable = false)
    private String paramValue;

    @Column(name = "param_name", length = 200, nullable = false)
    private String paramName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "status")
    private Integer status;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "udpated_date")  // Note: typo in original schema
    private LocalDateTime updatedDate;
}
