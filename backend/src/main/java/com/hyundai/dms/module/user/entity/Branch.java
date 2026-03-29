package com.hyundai.dms.module.user.entity;

import com.hyundai.dms.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "branches")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Branch extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "region", length = 50)
    private String region;

    @Column(name = "gstin", length = 15)
    private String gstin;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
