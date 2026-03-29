package com.hyundai.dms.module.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMenuRequest {

    private String name;
    private String path;
    private String icon;
    private Long parentId;
    private Integer displayOrder;
    private Boolean isActive;
}
