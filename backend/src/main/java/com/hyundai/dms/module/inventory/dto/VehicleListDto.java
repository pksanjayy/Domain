package com.hyundai.dms.module.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleListDto {
    private Long id;
    private String vin;
    private String brand;
    private String model;
    private String variant;
    private String colour;
    private String status;
    private Integer ageDays;
    private String branchName;
}
