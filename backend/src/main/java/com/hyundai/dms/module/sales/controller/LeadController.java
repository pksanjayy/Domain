package com.hyundai.dms.module.sales.controller;

import com.hyundai.dms.common.ApiResponse;
import com.hyundai.dms.common.PageResponse;
import com.hyundai.dms.common.filter.FilterRequest;
import com.hyundai.dms.module.sales.dto.CreateLeadRequest;
import com.hyundai.dms.module.sales.dto.LeadDto;
import com.hyundai.dms.module.sales.dto.StageTransitionRequest;
import com.hyundai.dms.module.sales.service.LeadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales/leads")
@RequiredArgsConstructor
@Tag(name = "Lead Management", description = "Sales lead CRUD and pipeline stage transitions")
public class LeadController {

    private final LeadService leadService;

    @GetMapping
    @Operation(summary = "List leads", description = "Paginated, filtered lead list")
    public ResponseEntity<ApiResponse<PageResponse<LeadDto>>> listLeads(FilterRequest filterRequest) {
        PageResponse<LeadDto> response = leadService.listLeads(filterRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/filter")
    @Operation(summary = "Filter leads", description = "POST-based paginated, filtered lead list")
    public ResponseEntity<ApiResponse<PageResponse<LeadDto>>> filterLeads(
            @RequestBody FilterRequest filterRequest) {
        PageResponse<LeadDto> response = leadService.listLeads(filterRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get lead by ID")
    public ResponseEntity<ApiResponse<LeadDto>> getLeadById(@PathVariable Long id) {
        LeadDto lead = leadService.getLeadById(id);
        return ResponseEntity.ok(ApiResponse.success(lead));
    }

    @PostMapping
    @Operation(summary = "Create lead", description = "SALES_CRM_EXEC or SUPER_ADMIN only")
    public ResponseEntity<ApiResponse<LeadDto>> createLead(
            @Valid @RequestBody CreateLeadRequest request) {
        LeadDto lead = leadService.createLead(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(lead));
    }

    @PatchMapping("/{id}/stage")
    @Operation(summary = "Transition lead stage", description = "State machine enforced — forward-only with LOST from any non-terminal")
    public ResponseEntity<ApiResponse<LeadDto>> transitionStage(
            @PathVariable Long id, @Valid @RequestBody StageTransitionRequest request) {
        LeadDto lead = leadService.transitionStage(id, request);
        return ResponseEntity.ok(ApiResponse.success(lead));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update lead", description = "Update lead details")
    public ResponseEntity<ApiResponse<LeadDto>> updateLead(
            @PathVariable Long id, @Valid @RequestBody com.hyundai.dms.module.sales.dto.UpdateLeadRequest request) {
        LeadDto lead = leadService.updateLead(id, request);
        return ResponseEntity.ok(ApiResponse.success(lead));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete lead", description = "SUPER_ADMIN only")
    public ResponseEntity<ApiResponse<Void>> deleteLead(@PathVariable Long id) {
        leadService.deleteLead(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
