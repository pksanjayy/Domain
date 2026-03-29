package com.hyundai.dms.module.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdiChecklistDto {
    private Long id;
    private Long vehicleId;
    private String vehicleVin;
    private String overallStatus;
    private String completedByUsername;
    private LocalDateTime completedAt;
    private String remarks;
    private List<PdiChecklistItemDto> items;
}
