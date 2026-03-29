package com.hyundai.dms.module.inventory.mapper;

import com.hyundai.dms.module.inventory.dto.PdiChecklistDto;
import com.hyundai.dms.module.inventory.dto.PdiChecklistItemDto;
import com.hyundai.dms.module.inventory.entity.PdiChecklist;
import com.hyundai.dms.module.inventory.entity.PdiChecklistItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PdiMapper {

    PdiMapper INSTANCE = Mappers.getMapper(PdiMapper.class);

    @Mapping(target = "vehicleId", source = "vehicle.id")
    @Mapping(target = "vehicleVin", source = "vehicle.vin")
    @Mapping(target = "overallStatus", expression = "java(checklist.getOverallStatus().name())")
    @Mapping(target = "completedByUsername", source = "completedBy.username")
    @Mapping(target = "items", source = "items")
    PdiChecklistDto toDto(PdiChecklist checklist);

    @Mapping(target = "result", expression = "java(item.getResult().name())")
    PdiChecklistItemDto toItemDto(PdiChecklistItem item);

    List<PdiChecklistItemDto> toItemDtoList(List<PdiChecklistItem> items);
}
