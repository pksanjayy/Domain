package com.hyundai.dms.module.inventory.controller;

import com.hyundai.dms.common.ApiResponse;
import com.hyundai.dms.common.PageResponse;
import com.hyundai.dms.common.filter.FilterRequest;
import com.hyundai.dms.module.inventory.dto.*;
import com.hyundai.dms.module.inventory.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicle Management", description = "Vehicle inventory CRUD and dashboard")
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    @Operation(summary = "List vehicles", description = "Paginated, filtered vehicle list using Specification DSL")
    public ResponseEntity<ApiResponse<PageResponse<VehicleListDto>>> listVehicles(
            FilterRequest filterRequest) {
        PageResponse<VehicleListDto> response = vehicleService.listVehicles(filterRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/filter")
    @Operation(summary = "Filter vehicles", description = "POST-based paginated, filtered vehicle list")
    public ResponseEntity<ApiResponse<PageResponse<VehicleListDto>>> filterVehicles(
            @RequestBody FilterRequest filterRequest) {
            
        List<com.hyundai.dms.common.filter.FilterCriteria> mappedFilters = filterRequest.filters().stream().map(f -> {
            String field = f.field();
            if ("branchName".equals(field)) field = "branch.name";
            if ("arrivalDate".equals(field)) field = "createdAt";
            return new com.hyundai.dms.common.filter.FilterCriteria(field, f.operator(), f.value());
        }).toList();

        List<com.hyundai.dms.common.filter.SortCriteria> mappedSorts = filterRequest.sorts().stream().map(s -> {
            String field = s.field();
            if ("branchName".equals(field)) field = "branch.name";
            if ("arrivalDate".equals(field)) field = "createdAt";
            return new com.hyundai.dms.common.filter.SortCriteria(field, s.direction());
        }).toList();

        FilterRequest mappedRequest = new FilterRequest(mappedFilters, filterRequest.page(), filterRequest.size(), mappedSorts);

        PageResponse<VehicleListDto> response = vehicleService.listVehicles(mappedRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vehicle by ID", description = "Full vehicle detail including GRN and accessories")
    public ResponseEntity<ApiResponse<VehicleDetailDto>> getVehicleById(@PathVariable Long id) {
        VehicleDetailDto vehicle = vehicleService.getVehicleById(id);
        // ETag is handled by ShallowEtagHeaderFilter
        return ResponseEntity.ok(ApiResponse.success(vehicle));
    }

    @GetMapping("/vin/{vin}")
    @Operation(summary = "Lookup vehicle by VIN")
    public ResponseEntity<ApiResponse<VehicleDetailDto>> getVehicleByVin(@PathVariable String vin) {
        VehicleDetailDto vehicle = vehicleService.getVehicleByVin(vin);
        return ResponseEntity.ok(ApiResponse.success(vehicle));
    }

    @PostMapping
    @Operation(summary = "Create vehicle", description = "WORKSHOP_EXEC or SUPER_ADMIN only")
    public ResponseEntity<ApiResponse<VehicleDetailDto>> createVehicle(
            @Valid @RequestBody CreateVehicleRequest request) {
        VehicleDetailDto vehicle = vehicleService.createVehicle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(vehicle));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update vehicle", description = "VIN is immutable and cannot be changed")
    public ResponseEntity<ApiResponse<VehicleDetailDto>> updateVehicle(
            @PathVariable Long id, @Valid @RequestBody UpdateVehicleRequest request) {
        VehicleDetailDto vehicle = vehicleService.updateVehicle(id, request);
        return ResponseEntity.ok(ApiResponse.success(vehicle));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Transition vehicle status", description = "State machine enforced — role-checked by service")
    public ResponseEntity<ApiResponse<VehicleDetailDto>> transitionStatus(
            @PathVariable Long id, @Valid @RequestBody StatusTransitionRequest request) {
        VehicleDetailDto vehicle = vehicleService.transitionStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(vehicle));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete vehicle", description = "SUPER_ADMIN only — soft delete")
    public ResponseEntity<ApiResponse<Void>> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/dashboard-summary")
    @Operation(summary = "Dashboard summary", description = "Total stock, status counts, ageing buckets, branch breakdown")
    public ResponseEntity<ApiResponse<DashboardSummaryDto>> getDashboardSummary() {
        DashboardSummaryDto summary = vehicleService.getDashboardSummary();
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/search")
    @Operation(summary = "Search vehicles by VIN", description = "Partial VIN match for autocomplete dropdowns")
    public ResponseEntity<ApiResponse<List<VehicleListDto>>> searchByVin(@RequestParam String vin) {
        List<VehicleListDto> vehicles = vehicleService.searchByVin(vin);
        return ResponseEntity.ok(ApiResponse.success(vehicles));
    }

    @GetMapping("/export")
    @Operation(summary = "Export vehicles", description = "Download vehicle inventory as CSV")
    public ResponseEntity<byte[]> exportVehicles() {
        byte[] csv = vehicleService.exportVehiclesCsv();
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=vehicles-export.csv")
                .header("Content-Type", "text/csv")
                .body(csv);
    }

    @GetMapping("/models")
    @Operation(summary = "Available models", description = "Cached list of distinct brand+model combinations currently available")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getAvailableModels() {
        List<Map<String, String>> models = vehicleService.getAvailableModels();
        return ResponseEntity.ok(ApiResponse.success(models));
    }
}
