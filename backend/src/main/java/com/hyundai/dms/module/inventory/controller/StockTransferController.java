package com.hyundai.dms.module.inventory.controller;

import com.hyundai.dms.common.ApiResponse;
import com.hyundai.dms.common.PageResponse;
import com.hyundai.dms.common.filter.FilterRequest;
import com.hyundai.dms.module.inventory.dto.RequestTransferRequest;
import com.hyundai.dms.module.inventory.dto.StockTransferDto;
import com.hyundai.dms.module.inventory.service.StockTransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory/transfers")
@RequiredArgsConstructor
@Tag(name = "Stock Transfer", description = "Inter-branch stock transfer operations")
public class StockTransferController {

    private final StockTransferService stockTransferService;

    @PostMapping
    @Operation(summary = "Request stock transfer")
    public ResponseEntity<ApiResponse<StockTransferDto>> requestTransfer(
            @Valid @RequestBody RequestTransferRequest request) {
        StockTransferDto transfer = stockTransferService.requestTransfer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(transfer));
    }

    @PatchMapping("/{id}/approve")
    @Operation(summary = "Approve transfer", description = "MANAGER_VIEWER only")
    public ResponseEntity<ApiResponse<StockTransferDto>> approveTransfer(@PathVariable Long id) {
        StockTransferDto transfer = stockTransferService.approveTransfer(id);
        return ResponseEntity.ok(ApiResponse.success(transfer));
    }

    @PatchMapping("/{id}/reject")
    @Operation(summary = "Reject transfer", description = "MANAGER_VIEWER only")
    public ResponseEntity<ApiResponse<StockTransferDto>> rejectTransfer(
            @PathVariable Long id, @RequestBody(required = false) java.util.Map<String, String> body) {
        String remarks = body != null ? body.getOrDefault("remarks", "") : "";
        StockTransferDto transfer = stockTransferService.rejectTransfer(id, remarks);
        return ResponseEntity.ok(ApiResponse.success(transfer));
    }

    @PostMapping("/filter")
    @Operation(summary = "Filter transfers", description = "POST-based paginated, filtered transfer list")
    public ResponseEntity<ApiResponse<PageResponse<StockTransferDto>>> filterTransfers(
            @RequestBody FilterRequest filterRequest) {
        PageResponse<StockTransferDto> response = stockTransferService.listTransfers(filterRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "List transfers", description = "Paginated, filtered list of stock transfers")
    public ResponseEntity<ApiResponse<PageResponse<StockTransferDto>>> listTransfers(
            FilterRequest filterRequest) {
        PageResponse<StockTransferDto> response = stockTransferService.listTransfers(filterRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
