package com.hyundai.dms.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuDto {

    private Long id;
    private String name;
    private String path;
    private String icon;
    private Integer displayOrder;
    private Long parentId;
    private List<MenuDto> children;
}
