package com.hyundai.dms.module.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long id;
    private String username;
    private String email;
    private List<RoleInfo> roles;
    private String branchName;
    private Long branchId;
    private Boolean isActive;
    private Integer failedLoginAttempts;
    private LocalDateTime lockedAt;
    private Boolean forcePasswordChange;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleInfo {
        private Long id;
        private String name;
        private String displayName;
    }
}
