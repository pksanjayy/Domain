package com.hyundai.dms.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMenuRequest {

    @NotBlank(message = "Menu name is required")
    private String name;

    private String path;
    private String icon;
    private Long parentId;
    private Integer displayOrder;
    private Boolean isActive = true;
}
