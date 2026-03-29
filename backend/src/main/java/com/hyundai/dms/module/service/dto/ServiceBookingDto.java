package com.hyundai.dms.module.service.dto;

import com.hyundai.dms.module.service.enums.ServiceBookingStatus;
import com.hyundai.dms.module.service.enums.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceBookingDto {
    private Long id;
    private Long branchId;
    private String branchName;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String customerMobile;
    private String bookingId;
    private LocalDate bookingDate;
    private LocalDate preferredServiceDate;
    private ServiceType serviceType;
    private String complaints;
    private ServiceBookingStatus status;
}
