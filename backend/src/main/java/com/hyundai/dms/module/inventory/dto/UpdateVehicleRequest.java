package com.hyundai.dms.module.inventory.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVehicleRequest {

    @NotBlank(message = "Brand is required")
    @Size(max = 50)
    private String brand;

    @NotBlank(message = "Model is required")
    @Size(max = 100)
    private String model;

    @NotBlank(message = "Variant is required")
    @Size(max = 100)
    private String variant;

    @Size(max = 50)
    private String colour;

    @NotNull(message = "Fuel type is required")
    private String fuelType;

    @NotNull(message = "Transmission is required")
    private String transmission;

    @NotNull(message = "Manufactured date is required")
    private java.time.LocalDate manufacturedDate;

    @NotNull(message = "MSRP is required")
    @Positive
    private BigDecimal msrp;

    @Size(max = 30)
    private String engineNumber;

    @Size(max = 30)
    private String chassisNumber;

    @Size(max = 20)
    private String keyNumber;

    @Size(max = 20)
    private String exteriorColourCode;

    @Size(max = 20)
    private String interiorColourCode;

    @NotNull(message = "Branch ID is required")
    private Long branchId;
}
