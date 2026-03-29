package com.hyundai.dms.module.inventory.entity;

import com.hyundai.dms.common.BaseEntity;
import com.hyundai.dms.module.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_accessories")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleAccessory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(name = "fitted_at")
    private LocalDateTime fittedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fitted_by")
    private User fittedBy;
}
