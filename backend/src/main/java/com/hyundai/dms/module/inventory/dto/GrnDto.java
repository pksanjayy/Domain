package com.hyundai.dms.module.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrnDto {
    private Long id;
    private String grnNumber;
    private Long vehicleId;
    private String vehicleVin;
    private String transporterName;
    private LocalDate dispatchDate;
    private LocalDate receivedDate;
    private String conditionOnArrival;
    private String remarks;
    private String receivedByUsername;
    private String branchName;
}
