package com.hyundai.dms.module.inventory.controller;

import com.hyundai.dms.common.ApiResponse;
import com.hyundai.dms.module.inventory.dto.PdiChecklistDto;
import com.hyundai.dms.module.inventory.dto.PdiChecklistItemDto;
import com.hyundai.dms.module.inventory.dto.UpdatePdiItemRequest;
import com.hyundai.dms.module.inventory.service.PdiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory/pdi")
@RequiredArgsConstructor
@Tag(name = "PDI Management", description = "Pre-Delivery Inspection checklist operations")
public class PdiController {

    private final PdiService pdiService;

    @PostMapping("/vehicle/{vehicleId}")
    @Operation(summary = "Create PDI checklist", description = "Creates checklist with default inspection points and transitions vehicle to PDI_PENDING")
    public ResponseEntity<ApiResponse<PdiChecklistDto>> createChecklist(@PathVariable Long vehicleId) {
        PdiChecklistDto checklist = pdiService.createChecklist(vehicleId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(checklist));
    }

    @PutMapping("/{checklistId}/item/{itemId}")
    @Operation(summary = "Update PDI item", description = "Update single PDI checklist item result + photo")
    public ResponseEntity<ApiResponse<PdiChecklistItemDto>> updateChecklistItem(
            @PathVariable Long checklistId, @PathVariable Long itemId,
            @Valid @RequestBody UpdatePdiItemRequest request) {
        PdiChecklistItemDto item = pdiService.updateChecklistItem(checklistId, itemId, request);
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    @PostMapping("/{checklistId}/complete")
    @Operation(summary = "Complete PDI checklist", description = "Finalizes checklist; if all items PASS, vehicle auto-transitions to PDI_DONE")
    public ResponseEntity<ApiResponse<PdiChecklistDto>> completeChecklist(@PathVariable Long checklistId) {
        PdiChecklistDto checklist = pdiService.completeChecklist(checklistId);
        return ResponseEntity.ok(ApiResponse.success(checklist));
    }

    @GetMapping("/vehicle/{vehicleId}")
    @Operation(summary = "Get PDI checklist by vehicle ID")
    public ResponseEntity<ApiResponse<PdiChecklistDto>> getChecklistByVehicle(@PathVariable Long vehicleId) {
        PdiChecklistDto checklist = pdiService.getChecklistByVehicleId(vehicleId);
        return ResponseEntity.ok(ApiResponse.success(checklist));
    }
}
