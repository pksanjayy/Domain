package com.hyundai.dms.module.sales.controller;

import com.hyundai.dms.common.ApiResponse;
import com.hyundai.dms.module.sales.dto.PaymentDto;
import com.hyundai.dms.module.sales.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentDto>> createPayment(@RequestBody PaymentDto paymentDto) {
        PaymentDto created = paymentService.createPayment(paymentDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentDto>> updatePayment(@PathVariable Long id, @RequestBody PaymentDto paymentDto) {
        PaymentDto updated = paymentService.updatePayment(id, paymentDto);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentDto>> getPayment(@PathVariable Long id) {
        PaymentDto payment = paymentService.getPayment(id);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<ApiResponse<List<PaymentDto>>> getAllPaymentsByBranch(@PathVariable Long branchId) {
        List<PaymentDto> payments = paymentService.getAllPaymentsByBranch(branchId);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<PaymentDto>>> filterPayments(
            @RequestParam Long branchId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String status) {
        List<PaymentDto> payments = paymentService.filterPayments(branchId, customerId, status);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
