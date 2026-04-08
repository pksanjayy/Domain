package com.hyundai.dms.module.user.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @NotEmpty(message = "At least one role must be assigned")
    private List<Long> roleIds;

    private Long branchId;

    private Boolean isActive;
}
