package com.hyundai.dms.module.inventory.dto;

import com.hyundai.dms.common.validation.ValidVin;
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
public class CreateVehicleRequest {

    @NotBlank(message = "VIN is required")
    @ValidVin
    private String vin;

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
    @Positive(message = "MSRP must be positive")
    private BigDecimal msrp;

    @Size(max = 30, message = "Engine number cannot exceed 30 characters")
    private String engineNumber;

    @Size(max = 30, message = "Chassis number cannot exceed 30 characters")
    private String chassisNumber;

    @Size(max = 20, message = "Key number cannot exceed 20 characters")
    private String keyNumber;

    @Size(max = 20, message = "Exterior colour code cannot exceed 20 characters")
    private String exteriorColourCode;

    @Size(max = 20, message = "Interior colour code cannot exceed 20 characters")
    private String interiorColourCode;

    @NotNull(message = "Branch ID is required")
    private Long branchId;
}
