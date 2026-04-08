package com.hyundai.dms.module.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleModelDto {
    private Long id;
    private String brand;
    private String model;
    private String displayName; // "Brand Model" for dropdown
    private Boolean isActive;
    private Integer vehicleCount;
}
