package com.hyundai.dms.module.service.dto;

import com.hyundai.dms.module.service.enums.PaymentStatus;
import com.hyundai.dms.module.service.enums.ServiceRecordStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRecordDto {
    private Long id;
    private Long branchId;
    private String branchName;
    private Long serviceBookingId;
    private String serviceBookingRef;
    private LocalDate serviceDate;
    private BigDecimal odometer;
    private String workPerformed;
    private String partsUsed;
    private Integer noOfTechnicians;
    private BigDecimal technicianHours;
    private String notes;
    private ServiceRecordStatus status;
    private PaymentStatus paymentStatus;
}
