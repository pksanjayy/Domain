package com.hyundai.dms.module.testdrive.dto;

import com.hyundai.dms.module.testdrive.enums.TestDriveBookingStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class TestDriveBookingDto {
    private Long id;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    
    private Long fleetId;
    private String fleetVin;
    private String fleetModel;
    
    private String bookingId;
    
    private Long salesExecutiveId;
    private String salesExecutiveName;
    
    private LocalDate bookingDate;
    private LocalDate testDriveDate;
    private LocalTime timeSlot;
    private String licenseNumber;
    private Boolean pickupRequired;
    private TestDriveBookingStatus status;
}
