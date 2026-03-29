package com.hyundai.dms.module.testdrive.mapper;

import com.hyundai.dms.module.testdrive.dto.TestDriveFleetDto;
import com.hyundai.dms.module.testdrive.entity.TestDriveFleet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TestDriveFleetMapper {

    @Mapping(source = "branch.id", target = "branchId")
    @Mapping(source = "branch.name", target = "branchName")
    TestDriveFleetDto toDto(TestDriveFleet entity);

    @Mapping(target = "branch", ignore = true)
    TestDriveFleet toEntity(TestDriveFleetDto dto);
}
