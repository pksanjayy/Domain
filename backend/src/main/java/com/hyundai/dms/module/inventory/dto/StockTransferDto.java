package com.hyundai.dms.module.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTransferDto {
    private Long id;
    private Long vehicleId;
    private String vehicleVin;
    private String fromBranchName;
    private String toBranchName;
    private String requestedByUsername;
    private String approvedByUsername;
    private String status;
    private LocalDateTime requestDate;
    private LocalDateTime approvalDate;
    private String remarks;
}
