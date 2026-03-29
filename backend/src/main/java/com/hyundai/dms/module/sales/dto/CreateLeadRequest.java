package com.hyundai.dms.module.sales.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeadRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    private Long assignedToId;

    @Size(max = 100)
    private String modelInterested;

    @NotBlank(message = "Source is required")
    private String source;

    private Long vehicleId;

    private Long branchId;
}
