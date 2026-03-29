package com.hyundai.dms.module.service.mapper;

import com.hyundai.dms.module.service.dto.ServiceBookingDto;
import com.hyundai.dms.module.service.entity.ServiceBooking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ServiceBookingMapper {

    ServiceBookingMapper INSTANCE = Mappers.getMapper(ServiceBookingMapper.class);

    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "branchName", source = "branch.name")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "customerEmail", source = "customer.email")
    @Mapping(target = "customerMobile", source = "customer.mobile")
    ServiceBookingDto toDto(ServiceBooking booking);

    List<ServiceBookingDto> toDtoList(List<ServiceBooking> bookings);

    // Ignored in toEntity mapping since references are resolved in Service layer
    @Mapping(target = "branch", ignore = true)
    @Mapping(target = "customer", ignore = true)
    ServiceBooking toEntity(ServiceBookingDto dto);
}
