package com.hyundai.dms.module.sales.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StageTransitionRequest {

    @NotBlank(message = "New stage is required")
    private String newStage;

    private String lostReason;
}
