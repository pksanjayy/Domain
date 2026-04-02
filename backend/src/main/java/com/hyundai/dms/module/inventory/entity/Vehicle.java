package com.hyundai.dms.module.inventory.entity;

import com.hyundai.dms.common.BaseEntity;
import com.hyundai.dms.module.inventory.enums.FuelType;
import com.hyundai.dms.module.inventory.enums.StockStatus;
import com.hyundai.dms.module.inventory.enums.TransmissionType;
import com.hyundai.dms.module.user.entity.Branch;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vehicles", indexes = {
        @Index(name = "idx_vehicles_vin", columnList = "vin"),
        @Index(name = "idx_vehicles_branch_status", columnList = "branch_id, status")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle extends BaseEntity {

    @Column(name = "vin", nullable = false, unique = true, length = 17)
    private String vin;

    @Column(name = "brand", nullable = false, length = 50)
    private String brand;

    @Column(name = "model", nullable = false, length = 100)
    private String model;

    @Column(name = "variant", nullable = false, length = 100)
    private String variant;

    @Column(name = "colour", length = 50)
    private String colour;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", nullable = false)
    private FuelType fuelType;

    @Enumerated(EnumType.STRING)
    @Column(name = "transmission", nullable = false)
    private TransmissionType transmission;

    @Column(name = "manufactured_date", nullable = false)
    private java.time.LocalDate manufacturedDate;

    @Column(name = "msrp", nullable = false, precision = 12, scale = 2)
    private BigDecimal msrp;

    @Column(name = "engine_number", length = 30)
    private String engineNumber;

    @Column(name = "chassis_number", length = 30)
    private String chassisNumber;

    @Column(name = "key_number", length = 20)
    private String keyNumber;

    @Column(name = "exterior_colour_code", length = 20)
    private String exteriorColourCode;

    @Column(name = "interior_colour_code", length = 20)
    private String interiorColourCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private StockStatus status = StockStatus.IN_TRANSIT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "age_days", nullable = false)
    @Builder.Default
    private Integer ageDays = 0;

    @Version
    @Column(name = "version")
    private Integer version;

    // ── Relationships ──

    @OneToOne(mappedBy = "vehicle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private GrnRecord grnRecord;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<VehicleAccessory> accessories = new ArrayList<>();

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<StockTransfer> transfers = new ArrayList<>();
}
