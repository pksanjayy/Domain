package com.hyundai.dms.module.inventory.mapper;

import com.hyundai.dms.module.inventory.dto.GrnDto;
import com.hyundai.dms.module.inventory.entity.GrnRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface GrnMapper {

    GrnMapper INSTANCE = Mappers.getMapper(GrnMapper.class);

    @Mapping(target = "vehicleId", source = "vehicle.id")
    @Mapping(target = "vehicleVin", source = "vehicle.vin")
    @Mapping(target = "conditionOnArrival", expression = "java(grn.getConditionOnArrival().name())")
    @Mapping(target = "receivedByUsername", source = "receivedBy.username")
    @Mapping(target = "branchName", source = "branch.name")
    GrnDto toDto(GrnRecord grn);
}
