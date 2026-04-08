package com.hyundai.dms.module.sales.mapper;

import com.hyundai.dms.module.sales.dto.LeadDto;
import com.hyundai.dms.module.sales.entity.Lead;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LeadMapper {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "customerMobile", source = "customer.mobile")
    @Mapping(target = "assignedToId", source = "assignedTo.id")
    @Mapping(target = "assignedToUsername", source = "assignedTo.username")
    @Mapping(target = "source", expression = "java(lead.getSource().name())")
    @Mapping(target = "stage", expression = "java(lead.getStage().name())")
    @Mapping(target = "vehicleId", source = "vehicle.id")
    @Mapping(target = "vehicleVin", source = "vehicle.vin")
    @Mapping(target = "vehicleModel", expression = "java(lead.getVehicle() != null ? lead.getVehicle().getBrand() + \" \" + lead.getVehicle().getModel() : null)")
    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "branchName", source = "branch.name")
    LeadDto toDto(Lead lead);
}
