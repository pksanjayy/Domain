package com.hyundai.dms.module.inventory.mapper;

import com.hyundai.dms.module.inventory.dto.StockTransferDto;
import com.hyundai.dms.module.inventory.entity.StockTransfer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface StockTransferMapper {

    StockTransferMapper INSTANCE = Mappers.getMapper(StockTransferMapper.class);

    @Mapping(target = "vehicleId", source = "vehicle.id")
    @Mapping(target = "vehicleVin", source = "vehicle.vin")
    @Mapping(target = "fromBranchName", source = "fromBranch.name")
    @Mapping(target = "toBranchName", source = "toBranch.name")
    @Mapping(target = "requestedByUsername", source = "requestedBy.username")
    @Mapping(target = "approvedByUsername", source = "approvedBy.username")
    @Mapping(target = "status", expression = "java(transfer.getStatus().name())")
    StockTransferDto toDto(StockTransfer transfer);
}
