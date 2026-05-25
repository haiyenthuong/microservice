package com.auth.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Entity representing Many-to-Many relationship giữa Group và Authority
 *
 * Bảng: adm_group_authorities
 *
 * Liên kết Group với Authority
 * Một Group có thể có nhiều Authorities
 * Một Authority có thể thuộc nhiều Groups
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
@Table(name = "adm_group_authorities")
public class GroupAuthority extends BaseEntity {

    /**
     * Group trong relation
     */
    @NotNull(message = "Group is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false, referencedColumnName = "id")
    private Group group;

    /**
     * Authority trong relation
     */
    @NotNull(message = "Authority is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authority_id", nullable = false, referencedColumnName = "id")
    private Authority authority;

    // Audit fields are inherited from BaseEntity
    // createdDate, updatedDate, createdBy, updatedBy
}
