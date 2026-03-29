package com.hyundai.dms.module.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleAccessoryDto {
    private Long id;
    private String name;
    private BigDecimal cost;
    private LocalDateTime fittedAt;
    private String fittedByUsername;
}
