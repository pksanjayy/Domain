package com.hyundai.dms.module.service.entity;

import com.hyundai.dms.common.BaseEntity;
import com.hyundai.dms.module.sales.entity.Customer;
import com.hyundai.dms.module.service.enums.ServiceBookingStatus;
import com.hyundai.dms.module.service.enums.ServiceType;
import com.hyundai.dms.module.user.entity.Branch;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "service_bookings", indexes = {
        @Index(name = "idx_sb_branch", columnList = "branch_id"),
        @Index(name = "idx_sb_customer", columnList = "customer_id"),
        @Index(name = "idx_sb_status", columnList = "status")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceBooking extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;



    @Column(name = "booking_id", nullable = false, unique = true, length = 50)
    private String bookingId;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(name = "preferred_service_date")
    private LocalDate preferredServiceDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false)
    private ServiceType serviceType;

    @Column(name = "complaints", columnDefinition = "TEXT")
    private String complaints;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ServiceBookingStatus status = ServiceBookingStatus.CONFIRMED;

    @Version
    @Column(name = "version")
    private Integer version;
}
