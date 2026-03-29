package com.hyundai.dms.module.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdiChecklistItemDto {
    private Long id;
    private String pointName;
    private String result;
    private String photoUrl;
    private String remark;
}
