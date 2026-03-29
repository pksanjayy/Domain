package com.hyundai.dms.module.sales.dto;

import com.hyundai.dms.module.sales.enums.PaymentMethod;
import com.hyundai.dms.module.sales.enums.SalesPaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class PaymentDto {
    private Long id;
    private Long customerId;
    private String customerName;
    private LocalDate paymentDate;
    private BigDecimal amountPaid;
    private BigDecimal totalPrice;
    private PaymentMethod paymentMethod;
    private String transactionId;
    private SalesPaymentStatus paymentStatus;
}
