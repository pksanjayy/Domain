package com.hyundai.dms.module.sales.entity;

import com.hyundai.dms.common.BaseEntity;
import com.hyundai.dms.module.user.entity.Branch;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "customers", indexes = {
        @Index(name = "idx_customer_mobile", columnList = "mobile"),
        @Index(name = "idx_customer_branch", columnList = "branch_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "mobile", nullable = false, unique = true, length = 10)
    private String mobile;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "location", length = 150)
    private String location;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;
}
