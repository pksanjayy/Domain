package com.hyundai.dms.module.testdrive.mapper;

import com.hyundai.dms.module.testdrive.dto.TestDriveFleetDto;
import com.hyundai.dms.module.testdrive.entity.TestDriveFleet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TestDriveFleetMapper {

    @Mapping(source = "branch.id", target = "branchId")
    @Mapping(source = "branch.name", target = "branchName")
    @Mapping(source = "vehicle.id", target = "vehicleId")
    @Mapping(target = "vehicleModel", expression = "java(entity.getVehicle() != null ? entity.getVehicle().getBrand() + \" \" + entity.getVehicle().getModel() : null)")
    @Mapping(target = "brand", expression = "java(entity.getVehicle() != null ? entity.getVehicle().getBrand() : null)")
    @Mapping(target = "model", expression = "java(entity.getVehicle() != null ? entity.getVehicle().getModel() : null)")
    TestDriveFleetDto toDto(TestDriveFleet entity);

    @Mapping(target = "branch", ignore = true)
    @Mapping(target = "vehicle", ignore = true)
    TestDriveFleet toEntity(TestDriveFleetDto dto);
}
