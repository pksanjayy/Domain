package com.hyundai.dms.module.sales.controller;

import com.hyundai.dms.common.ApiResponse;
import com.hyundai.dms.common.PageResponse;
import com.hyundai.dms.common.filter.FilterRequest;
import com.hyundai.dms.module.sales.dto.CreateCustomerRequest;
import com.hyundai.dms.module.sales.dto.CustomerDto;
import com.hyundai.dms.module.sales.dto.UpdateCustomerRequest;
import com.hyundai.dms.module.sales.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Management", description = "Sales customer CRUD")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    @Operation(summary = "List customers", description = "Paginated, filtered customer list")
    public ResponseEntity<ApiResponse<PageResponse<CustomerDto>>> listCustomers(FilterRequest filterRequest) {
        PageResponse<CustomerDto> response = customerService.listCustomers(filterRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/filter")
    @Operation(summary = "Filter customers", description = "POST-based paginated, filtered customer list")
    public ResponseEntity<ApiResponse<PageResponse<CustomerDto>>> filterCustomers(
            @RequestBody FilterRequest filterRequest) {
        PageResponse<CustomerDto> response = customerService.listCustomers(filterRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID")
    public ResponseEntity<ApiResponse<CustomerDto>> getCustomerById(@PathVariable Long id) {
        CustomerDto customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.success(customer));
    }

    @PostMapping
    @Operation(summary = "Create customer", description = "SALES_CRM_EXEC or SUPER_ADMIN only")
    public ResponseEntity<ApiResponse<CustomerDto>> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request) {
        CustomerDto customer = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(customer));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer", description = "Mobile is immutable")
    public ResponseEntity<ApiResponse<CustomerDto>> updateCustomer(
            @PathVariable Long id, @Valid @RequestBody UpdateCustomerRequest request) {
        CustomerDto customer = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(ApiResponse.success(customer));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete customer", description = "SUPER_ADMIN only")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
