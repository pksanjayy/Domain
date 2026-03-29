package com.hyundai.dms.module.service.entity;

import com.hyundai.dms.common.BaseEntity;
import com.hyundai.dms.module.service.enums.PaymentStatus;
import com.hyundai.dms.module.service.enums.ServiceRecordStatus;
import com.hyundai.dms.module.user.entity.Branch;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "service_records", indexes = {
        @Index(name = "idx_sr_branch", columnList = "branch_id"),
        @Index(name = "idx_sr_status", columnList = "status")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_booking_id", nullable = false, unique = true)
    private ServiceBooking serviceBooking;

    @Column(name = "service_date", nullable = false)
    private LocalDate serviceDate;

    @Column(name = "odometer", precision = 10, scale = 2)
    private BigDecimal odometer;

    @Column(name = "work_performed", columnDefinition = "TEXT")
    private String workPerformed;

    @Column(name = "parts_used", columnDefinition = "TEXT")
    private String partsUsed;

    @Column(name = "no_of_technicians")
    private Integer noOfTechnicians;

    @Column(name = "technician_hours", precision = 6, scale = 2)
    private BigDecimal technicianHours;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ServiceRecordStatus status = ServiceRecordStatus.IN_PROGRESS;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Version
    @Column(name = "version")
    private Integer version;
}
