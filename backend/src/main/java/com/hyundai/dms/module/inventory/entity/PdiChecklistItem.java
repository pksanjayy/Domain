package com.hyundai.dms.module.inventory.entity;

import com.hyundai.dms.common.BaseEntity;
import com.hyundai.dms.module.inventory.enums.PdiItemResult;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pdi_checklist_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdiChecklistItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checklist_id", nullable = false)
    private PdiChecklist checklist;

    @Column(name = "point_name", nullable = false, length = 100)
    private String pointName;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false)
    @Builder.Default
    private PdiItemResult result = PdiItemResult.NA;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(name = "remark", length = 255)
    private String remark;
}
