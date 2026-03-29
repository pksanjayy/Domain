package com.hyundai.dms.module.sales.service.impl;

import com.hyundai.dms.exception.ResourceNotFoundException;
import com.hyundai.dms.module.sales.dto.PaymentDto;
import com.hyundai.dms.module.sales.entity.Customer;
import com.hyundai.dms.module.sales.entity.Payment;
import com.hyundai.dms.module.sales.enums.SalesPaymentStatus;
import com.hyundai.dms.module.sales.mapper.PaymentMapper;
import com.hyundai.dms.module.sales.repository.CustomerRepository;
import com.hyundai.dms.module.sales.repository.PaymentRepository;
import com.hyundai.dms.module.sales.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional
    public PaymentDto createPayment(PaymentDto paymentDto) {
        Customer customer = customerRepository.findById(paymentDto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", paymentDto.getCustomerId()));

        Payment payment = paymentMapper.toEntity(paymentDto);
        payment.setCustomer(customer);
        if (payment.getPaymentStatus() == null) {
            payment.setPaymentStatus(SalesPaymentStatus.PENDING);
        }

        Payment savedPayment = paymentRepository.save(payment);
        return paymentMapper.toDto(savedPayment);
    }

    @Override
    @Transactional
    public PaymentDto updatePayment(Long id, PaymentDto paymentDto) {
        Payment existingPayment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", id));

        if (paymentDto.getCustomerId() != null && !paymentDto.getCustomerId().equals(existingPayment.getCustomer().getId())) {
            Customer customer = customerRepository.findById(paymentDto.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", paymentDto.getCustomerId()));
            existingPayment.setCustomer(customer);
        }

        paymentMapper.updateEntityFromDto(paymentDto, existingPayment);
        Payment savedPayment = paymentRepository.save(existingPayment);
        return paymentMapper.toDto(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDto getPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", id));
        return paymentMapper.toDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDto> getAllPaymentsByBranch(Long branchId) {
        return paymentRepository.findByCustomerBranchId(branchId).stream()
                .map(paymentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDto> filterPayments(Long branchId, Long customerId, String status) {
        // Simplified filter logic
        List<Payment> payments;
        if (customerId != null) {
            payments = paymentRepository.findByCustomerId(customerId);
        } else {
            payments = paymentRepository.findByCustomerBranchId(branchId);
        }

        if (status != null && !status.isEmpty()) {
            SalesPaymentStatus paymentStatus = SalesPaymentStatus.valueOf(status.toUpperCase());
            payments = payments.stream()
                    .filter(p -> p.getPaymentStatus() == paymentStatus)
                    .collect(Collectors.toList());
        }

        return payments.stream()
                .map(paymentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deletePayment(Long id) {
        if (!paymentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Payment", id);
        }
        paymentRepository.deleteById(id);
    }
}
