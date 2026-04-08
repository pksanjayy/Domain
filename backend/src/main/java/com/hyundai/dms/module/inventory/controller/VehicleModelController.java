package com.hyundai.dms.module.inventory.controller;

import com.hyundai.dms.common.ApiResponse;
import com.hyundai.dms.module.inventory.dto.VehicleModelDto;
import com.hyundai.dms.module.inventory.service.VehicleModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory/vehicle-models")
@RequiredArgsConstructor
@Tag(name = "Vehicle Models", description = "Vehicle model master data")
public class VehicleModelController {

    private final VehicleModelService vehicleModelService;

    @GetMapping
    @Operation(summary = "Get all active vehicle models", description = "Returns list of vehicle models currently in use")
    public ResponseEntity<ApiResponse<List<VehicleModelDto>>> getActiveModels() {
        List<VehicleModelDto> models = vehicleModelService.getAllActiveModelsDto();
        return ResponseEntity.ok(ApiResponse.success(models));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all vehicle models", description = "Returns all vehicle models including inactive ones")
    public ResponseEntity<ApiResponse<List<VehicleModelDto>>> getAllModels() {
        List<VehicleModelDto> models = vehicleModelService.getAllModelsDto();
        return ResponseEntity.ok(ApiResponse.success(models));
    }
}
