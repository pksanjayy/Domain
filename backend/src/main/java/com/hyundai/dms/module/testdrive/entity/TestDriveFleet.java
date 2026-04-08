package com.hyundai.dms.module.testdrive.entity;

import com.hyundai.dms.common.BaseEntity;
import com.hyundai.dms.module.inventory.entity.Vehicle;
import com.hyundai.dms.module.testdrive.enums.FuelType;
import com.hyundai.dms.module.testdrive.enums.TestDriveFleetStatus;
import com.hyundai.dms.module.testdrive.enums.Transmission;
import com.hyundai.dms.module.user.entity.Branch;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "test_drive_fleet", indexes = {
        @Index(name = "idx_test_drive_fleet_branch", columnList = "branch_id"),
        @Index(name = "idx_test_drive_fleet_status", columnList = "status"),
        @Index(name = "idx_test_drive_fleet_vehicle", columnList = "vehicle_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestDriveFleet extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Column(name = "fleet_id", nullable = false, unique = true, length = 50)
    private String fleetId;

    @Column(name = "vin", nullable = false, unique = true, length = 50)
    private String vin;

    @Column(name = "variant", nullable = false, length = 100)
    private String variant;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", nullable = false, length = 20)
    private FuelType fuelType;

    @Enumerated(EnumType.STRING)
    @Column(name = "transmission", nullable = false, length = 20)
    private Transmission transmission;

    @Column(name = "registration_number", nullable = false, length = 50)
    private String registrationNumber;

    @Column(name = "insurance_expiry")
    private LocalDate insuranceExpiry;

    @Column(name = "rc_expiry")
    private LocalDate rcExpiry;

    @Column(name = "current_odometer")
    private Integer currentOdometer;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TestDriveFleetStatus status = TestDriveFleetStatus.AVAILABLE;

    @Column(name = "last_service_date")
    private LocalDate lastServiceDate;

    @Column(name = "next_service_due")
    private LocalDate nextServiceDue;

    @Version
    @Column(name = "version")
    private Integer version;
}
