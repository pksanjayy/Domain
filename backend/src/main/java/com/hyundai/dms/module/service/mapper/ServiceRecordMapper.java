package com.hyundai.dms.module.service.mapper;

import com.hyundai.dms.module.service.dto.ServiceRecordDto;
import com.hyundai.dms.module.service.entity.ServiceRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ServiceRecordMapper {

    ServiceRecordMapper INSTANCE = Mappers.getMapper(ServiceRecordMapper.class);

    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "branchName", source = "branch.name")
    @Mapping(target = "serviceBookingId", source = "serviceBooking.id")
    @Mapping(target = "serviceBookingRef", source = "serviceBooking.bookingId")
    ServiceRecordDto toDto(ServiceRecord record);

    List<ServiceRecordDto> toDtoList(List<ServiceRecord> records);

    @Mapping(target = "branch", ignore = true)
    @Mapping(target = "serviceBooking", ignore = true)
    ServiceRecord toEntity(ServiceRecordDto dto);
}
