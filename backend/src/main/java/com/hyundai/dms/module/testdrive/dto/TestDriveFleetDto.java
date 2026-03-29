package com.hyundai.dms.module.testdrive.dto;

import com.hyundai.dms.module.testdrive.enums.FuelType;
import com.hyundai.dms.module.testdrive.enums.TestDriveFleetStatus;
import com.hyundai.dms.module.testdrive.enums.Transmission;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class TestDriveFleetDto {
    private Long id;
    private Long branchId;
    private String branchName;
    private String fleetId;
    private String vin;
    private String brand;
    private String model;
    private String variant;
    private FuelType fuelType;
    private Transmission transmission;
    private String registrationNumber;
    private LocalDate insuranceExpiry;
    private LocalDate rcExpiry;
    private Integer currentOdometer;
    private TestDriveFleetStatus status;
    private LocalDate lastServiceDate;
    private LocalDate nextServiceDue;
}
