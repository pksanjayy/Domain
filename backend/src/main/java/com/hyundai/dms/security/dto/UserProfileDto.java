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
public class UserProfileDto {

    private Long id;
    private String username;
    private String email;
    private List<String> roles;
    private Long branchId;
    private String branchName;
    private boolean forcePasswordChange;
    private List<MenuDto> menus;
    private List<PermissionDto> permissions;
}
