package com.hyundai.dms.module.testdrive.mapper;

import com.hyundai.dms.module.testdrive.dto.TestDriveBookingDto;
import com.hyundai.dms.module.testdrive.entity.TestDriveBooking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TestDriveBookingMapper {

    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "customer.name", target = "customerName")
    @Mapping(source = "customer.mobile", target = "customerPhone")
    @Mapping(source = "fleet.id", target = "fleetId")
    @Mapping(source = "fleet.vin", target = "fleetVin")
    @Mapping(target = "fleetModel", expression = "java(entity.getFleet() != null && entity.getFleet().getVehicle() != null ? entity.getFleet().getVehicle().getBrand() + \" \" + entity.getFleet().getVehicle().getModel() : null)")
    @Mapping(source = "salesExecutive.id", target = "salesExecutiveId")
    @Mapping(source = "salesExecutive.username", target = "salesExecutiveName")
    TestDriveBookingDto toDto(TestDriveBooking entity);

    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "fleet", ignore = true)
    @Mapping(target = "salesExecutive", ignore = true)
    TestDriveBooking toEntity(TestDriveBookingDto dto);
}
