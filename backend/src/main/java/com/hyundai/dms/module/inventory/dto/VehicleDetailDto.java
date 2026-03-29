package com.hyundai.dms.module.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDetailDto {
    private Long id;
    private String vin;
    private String brand;
    private String model;
    private String variant;
    private String colour;
    private String fuelType;
    private String transmission;
    private java.time.LocalDate manufacturedDate;
    private BigDecimal msrp;
    private String engineNumber;
    private String chassisNumber;
    private String keyNumber;
    private String exteriorColourCode;
    private String interiorColourCode;
    private String status;
    private Integer ageDays;
    private String branchName;
    private Long branchId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Nested details
    private GrnDto grnRecord;
    private PdiChecklistDto pdiChecklist;
    private List<VehicleAccessoryDto> accessories;
}
