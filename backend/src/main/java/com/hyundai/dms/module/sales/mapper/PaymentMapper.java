package com.hyundai.dms.module.sales.mapper;

import com.hyundai.dms.module.sales.dto.PaymentDto;
import com.hyundai.dms.module.sales.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "customer.name", target = "customerName")
    PaymentDto toDto(Payment payment);

    @Mapping(target = "customer", ignore = true)
    Payment toEntity(PaymentDto paymentDto);

    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(PaymentDto dto, @MappingTarget Payment entity);
}
