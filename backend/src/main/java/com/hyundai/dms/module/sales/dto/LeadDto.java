package com.hyundai.dms.module.sales.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LeadDto {

    private Long id;
    private Long customerId;
    private String customerName;
    private String customerMobile;
    private Long assignedToId;
    private String assignedToUsername;
    private String modelInterested;
    private String source;
    private String stage;
    private String lostReason;
    private Long vehicleId;
    private String vehicleVin;
    private Long branchId;
    private String branchName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
