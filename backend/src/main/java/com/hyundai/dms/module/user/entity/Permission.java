package com.hyundai.dms.module.user.entity;

import com.hyundai.dms.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permissions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"role_id", "module_name"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "module_name", nullable = false, length = 50)
    private String moduleName;

    @Column(name = "can_create", nullable = false)
    private Boolean canCreate;

    @Column(name = "can_read", nullable = false)
    private Boolean canRead;

    @Column(name = "can_update", nullable = false)
    private Boolean canUpdate;

    @Column(name = "can_delete", nullable = false)
    private Boolean canDelete;
}
