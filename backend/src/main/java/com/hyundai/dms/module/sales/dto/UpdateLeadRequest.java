package com.hyundai.dms.module.sales.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLeadRequest {

    private Long assignedToId;

    @Size(max = 100)
    private String modelInterested;

    @NotBlank(message = "Source is required")
    private String source;

    private Long vehicleId;

    private Long branchId;
}
