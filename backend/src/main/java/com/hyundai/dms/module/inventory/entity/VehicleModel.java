package com.hyundai.dms.module.inventory.entity;

import com.hyundai.dms.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vehicle_models", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"brand", "model"}),
    indexes = {
        @Index(name = "idx_vehicle_model_brand", columnList = "brand"),
        @Index(name = "idx_vehicle_model_active", columnList = "is_active")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleModel extends BaseEntity {

    @Column(name = "brand", nullable = false, length = 100)
    private String brand;

    @Column(name = "model", nullable = false, length = 100)
    private String model;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "vehicle_count", nullable = false)
    @Builder.Default
    private Integer vehicleCount = 0;
}
