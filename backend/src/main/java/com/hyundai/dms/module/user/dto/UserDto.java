package com.hyundai.dms.module.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long id;
    private String username;
    private String email;
    private String roleName;
    private Long roleId;
    private String branchName;
    private Long branchId;
    private Boolean isActive;
    private Integer failedLoginAttempts;
    private LocalDateTime lockedAt;
    private Boolean forcePasswordChange;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
