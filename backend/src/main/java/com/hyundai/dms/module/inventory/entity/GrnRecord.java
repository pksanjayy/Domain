package com.hyundai.dms.module.inventory.entity;

import com.hyundai.dms.common.BaseEntity;
import com.hyundai.dms.module.inventory.enums.ArrivalCondition;
import com.hyundai.dms.module.user.entity.Branch;
import com.hyundai.dms.module.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "grn_records")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrnRecord extends BaseEntity {

    @Column(name = "grn_number", nullable = false, unique = true, length = 20)
    private String grnNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "transporter_name", length = 100)
    private String transporterName;

    @Column(name = "dispatch_date")
    private LocalDate dispatchDate;

    @Column(name = "received_date", nullable = false)
    private LocalDate receivedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_on_arrival", nullable = false)
    private ArrivalCondition conditionOnArrival;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "received_by")
    private User receivedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;
}
