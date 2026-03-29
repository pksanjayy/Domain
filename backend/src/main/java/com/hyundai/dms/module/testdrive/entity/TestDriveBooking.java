package com.hyundai.dms.module.testdrive.entity;

import com.hyundai.dms.common.BaseEntity;
import com.hyundai.dms.module.sales.entity.Customer;
import com.hyundai.dms.module.testdrive.enums.TestDriveBookingStatus;
import com.hyundai.dms.module.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "test_drive_bookings", indexes = {
        @Index(name = "idx_tdb_customer", columnList = "customer_id"),
        @Index(name = "idx_tdb_fleet", columnList = "fleet_id"),
        @Index(name = "idx_tdb_sales_exec", columnList = "sales_executive_id"),
        @Index(name = "idx_tdb_status", columnList = "status")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestDriveBooking extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fleet_id", nullable = false)
    private TestDriveFleet fleet;

    @Column(name = "booking_id", nullable = false, unique = true, length = 50)
    private String bookingId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sales_executive_id")
    private User salesExecutive;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(name = "test_drive_date", nullable = false)
    private LocalDate testDriveDate;

    @Column(name = "time_slot", nullable = false)
    private LocalTime timeSlot;

    @Column(name = "license_number", length = 100)
    private String licenseNumber;

    @Column(name = "pickup_required")
    @Builder.Default
    private Boolean pickupRequired = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TestDriveBookingStatus status = TestDriveBookingStatus.BOOKED;

    @Version
    @Column(name = "version")
    private Integer version;
}
