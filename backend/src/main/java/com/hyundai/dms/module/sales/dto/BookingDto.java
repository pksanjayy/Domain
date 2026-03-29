package com.hyundai.dms.module.sales.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingDto {

    private Long id;
    private Long leadId;
    private String customerName;
    private Long vehicleId;
    private String vehicleVin;
    private String vehicleModel;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private LocalDate bookingDate;
    private LocalDate expectedDelivery;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
