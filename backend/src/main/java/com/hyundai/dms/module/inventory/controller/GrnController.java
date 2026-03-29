package com.hyundai.dms.module.inventory.controller;

import com.hyundai.dms.common.ApiResponse;
import com.hyundai.dms.common.PageResponse;
import com.hyundai.dms.common.filter.FilterRequest;
import com.hyundai.dms.module.inventory.dto.CreateGrnRequest;
import com.hyundai.dms.module.inventory.dto.UpdateGrnRequest;
import com.hyundai.dms.module.inventory.dto.GrnDto;
import com.hyundai.dms.module.inventory.service.GrnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory/grn")
@RequiredArgsConstructor
@Tag(name = "GRN Management", description = "Goods Receipt Note operations")
public class GrnController {

    private final GrnService grnService;

    @PostMapping("/filter")
    @Operation(summary = "Filter GRNs", description = "POST-based paginated, filtered GRN list")
    public ResponseEntity<ApiResponse<PageResponse<GrnDto>>> filterGrns(
            @RequestBody FilterRequest filterRequest) {
        PageResponse<GrnDto> response = grnService.listGrns(filterRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Receive GRN", description = "Create GRN record and transition vehicle to GRN_RECEIVED")
    public ResponseEntity<ApiResponse<GrnDto>> receiveGrn(@Valid @RequestBody CreateGrnRequest request) {
        GrnDto grn = grnService.receiveGrn(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(grn));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get GRN by ID")
    public ResponseEntity<ApiResponse<GrnDto>> getGrnById(@PathVariable Long id) {
        GrnDto grn = grnService.getGrnById(id);
        return ResponseEntity.ok(ApiResponse.success(grn));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update GRN", description = "Update GRN record details")
    public ResponseEntity<ApiResponse<GrnDto>> updateGrn(@PathVariable Long id, @Valid @RequestBody UpdateGrnRequest request) {
        GrnDto grn = grnService.updateGrn(id, request);
        return ResponseEntity.ok(ApiResponse.success(grn));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete GRN", description = "Delete GRN record and revert vehicle status")
    public ResponseEntity<ApiResponse<Void>> deleteGrn(@PathVariable Long id) {
        grnService.deleteGrn(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
