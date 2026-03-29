package com.hyundai.dms.module.sales.service;

import com.hyundai.dms.module.sales.dto.PaymentDto;

import java.util.List;

public interface PaymentService {
    PaymentDto createPayment(PaymentDto paymentDto);
    PaymentDto updatePayment(Long id, PaymentDto paymentDto);
    PaymentDto getPayment(Long id);
    List<PaymentDto> getAllPaymentsByBranch(Long branchId);
    List<PaymentDto> filterPayments(Long branchId, Long customerId, String status);
    void deletePayment(Long id);
}
