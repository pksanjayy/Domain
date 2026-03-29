package com.hyundai.dms.module.inventory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusTransitionRequest {

    @NotNull(message = "New status is required")
    private String newStatus;

    private String remarks;
}
