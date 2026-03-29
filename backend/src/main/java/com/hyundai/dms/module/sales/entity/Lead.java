package com.hyundai.dms.module.sales.entity;

import com.hyundai.dms.common.BaseEntity;
import com.hyundai.dms.module.inventory.entity.Vehicle;
import com.hyundai.dms.module.sales.enums.LeadSource;
import com.hyundai.dms.module.sales.enums.LeadStage;
import com.hyundai.dms.module.user.entity.Branch;
import com.hyundai.dms.module.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "leads", indexes = {
        @Index(name = "idx_lead_customer", columnList = "customer_id"),
        @Index(name = "idx_lead_assigned", columnList = "assigned_to"),
        @Index(name = "idx_lead_stage", columnList = "stage"),
        @Index(name = "idx_lead_branch", columnList = "branch_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lead extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to", nullable = false)
    private User assignedTo;

    @Column(name = "model_interested", length = 100)
    private String modelInterested;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    private LeadSource source;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage", nullable = false)
    @Builder.Default
    private LeadStage stage = LeadStage.NEW_LEAD;

    @Column(name = "lost_reason", length = 255)
    private String lostReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;
}
