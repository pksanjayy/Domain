package com.hyundai.dms.module.sales.mapper;

import com.hyundai.dms.module.sales.dto.CustomerDto;
import com.hyundai.dms.module.sales.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "branchName", source = "branch.name")
    CustomerDto toDto(Customer customer);
}
