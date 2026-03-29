package com.hyundai.dms.module.user.dto;

import com.hyundai.dms.security.dto.MenuDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDto {

    private Long id;
    private String name;
    private String displayName;
    private String description;
    private List<Map<String, Object>> permissions;
    private List<MenuDto> menus;
    private List<Long> menuIds;
}
