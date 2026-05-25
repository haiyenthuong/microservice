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
 * Entity representing Many-to-Many relationship giữa Group và User
 *
 * Bảng: adm_group_users
 *
 * Liên kết Group với User
 * Một User có thể thuộc nhiều Groups
 * Một Group có thể chứa nhiều Users
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
@Table(name = "adm_group_users")
public class GroupUser extends BaseEntity {

    /**
     * User trong relation
     */
    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    private User user;

    /**
     * Group trong relation
     */
    @NotNull(message = "Group is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false, referencedColumnName = "id")
    private Group group;

    // Audit fields are inherited from BaseEntity
    // createdDate, updatedDate, createdBy, updatedBy
}
