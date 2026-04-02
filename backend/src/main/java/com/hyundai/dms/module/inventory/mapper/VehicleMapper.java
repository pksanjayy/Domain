package com.hyundai.dms.module.inventory.mapper;

import com.hyundai.dms.module.inventory.dto.VehicleAccessoryDto;
import com.hyundai.dms.module.inventory.dto.VehicleDetailDto;
import com.hyundai.dms.module.inventory.dto.VehicleListDto;
import com.hyundai.dms.module.inventory.entity.Vehicle;
import com.hyundai.dms.module.inventory.entity.VehicleAccessory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", uses = {GrnMapper.class})
public interface VehicleMapper {

    VehicleMapper INSTANCE = Mappers.getMapper(VehicleMapper.class);

    @Mapping(target = "branchName", source = "branch.name")
    @Mapping(target = "status", expression = "java(vehicle.getStatus().name())")
    @Mapping(target = "ageDays", expression = "java(vehicle.getManufacturedDate() != null ? (int) java.time.temporal.ChronoUnit.DAYS.between(vehicle.getManufacturedDate(), java.time.LocalDate.now()) : 0)")
    VehicleListDto toListDto(Vehicle vehicle);

    @Mapping(target = "branchName", source = "branch.name")
    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "status", expression = "java(vehicle.getStatus().name())")
    @Mapping(target = "fuelType", expression = "java(vehicle.getFuelType().name())")
    @Mapping(target = "transmission", expression = "java(vehicle.getTransmission().name())")
    @Mapping(target = "ageDays", expression = "java(vehicle.getManufacturedDate() != null ? (int) java.time.temporal.ChronoUnit.DAYS.between(vehicle.getManufacturedDate(), java.time.LocalDate.now()) : 0)")
    @Mapping(target = "grnRecord", source = "grnRecord")
    @Mapping(target = "accessories", source = "accessories")
    VehicleDetailDto toDetailDto(Vehicle vehicle);

    @Mapping(target = "fittedByUsername", source = "fittedBy.username")
    VehicleAccessoryDto toAccessoryDto(VehicleAccessory accessory);

    List<VehicleAccessoryDto> toAccessoryDtoList(List<VehicleAccessory> accessories);
}
