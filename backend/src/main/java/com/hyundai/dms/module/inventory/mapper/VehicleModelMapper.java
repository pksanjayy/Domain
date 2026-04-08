package com.hyundai.dms.module.inventory.mapper;

import com.hyundai.dms.module.inventory.dto.VehicleModelDto;
import com.hyundai.dms.module.inventory.entity.VehicleModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VehicleModelMapper {

    @Mapping(target = "displayName", expression = "java(vehicleModel.getBrand() + \" \" + vehicleModel.getModel())")
    VehicleModelDto toDto(VehicleModel vehicleModel);

    List<VehicleModelDto> toDtoList(List<VehicleModel> vehicleModels);
}
