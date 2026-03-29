package com.hyundai.dms.module.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGrnRequest {

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    private String transporterName;

    private LocalDate dispatchDate;

    @NotNull(message = "Received date is required")
    private LocalDate receivedDate;

    @NotBlank(message = "Condition on arrival is required")
    private String conditionOnArrival;

    private String remarks;
}
