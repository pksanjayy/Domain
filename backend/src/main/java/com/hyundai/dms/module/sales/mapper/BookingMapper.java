package com.hyundai.dms.module.sales.mapper;

import com.hyundai.dms.module.sales.dto.BookingDto;
import com.hyundai.dms.module.sales.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "leadId", source = "lead.id")
    @Mapping(target = "customerName", source = "lead.customer.name")
    @Mapping(target = "vehicleId", source = "vehicle.id")
    @Mapping(target = "vehicleVin", source = "vehicle.vin")
    @Mapping(target = "vehicleModel", expression = "java(booking.getVehicle().getBrand() + \" \" + booking.getVehicle().getModel())")
    @Mapping(target = "status", expression = "java(booking.getStatus().name())")
    BookingDto toDto(Booking booking);
}
