package com.hyundai.dms.module.user.controller;

import com.hyundai.dms.common.ApiResponse;
import com.hyundai.dms.common.PageResponse;
import com.hyundai.dms.module.user.dto.BranchDto;
import com.hyundai.dms.module.user.dto.CreateBranchRequest;
import com.hyundai.dms.module.user.dto.UpdateBranchRequest;
import com.hyundai.dms.module.user.service.BranchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/branches")
@RequiredArgsConstructor
@Tag(name = "Branch Management", description = "Branch CRUD endpoints")
public class BranchController {

    private final BranchService branchService;

    @GetMapping
    @Operation(summary = "List all branches (paginated)", description = "Paginated list with optional search and status filter")
    public ResponseEntity<ApiResponse<PageResponse<BranchDto>>> getAllBranches(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        
        return ResponseEntity.ok(ApiResponse.success(branchService.getAllBranches(search, isActive, pageable)));
    }

    @GetMapping("/dropdown")
    @Operation(summary = "List all active branches for dropdowns", description = "Non-paginated list of active branches")
    public ResponseEntity<ApiResponse<List<BranchDto>>> getBranchDropdown() {
        return ResponseEntity.ok(ApiResponse.success(branchService.getBranchDropdownList()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get branch by ID")
    public ResponseEntity<ApiResponse<BranchDto>> getBranchById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(branchService.getBranchById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Create branch")
    public ResponseEntity<ApiResponse<BranchDto>> createBranch(@Valid @RequestBody CreateBranchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(branchService.createBranch(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Update branch")
    public ResponseEntity<ApiResponse<BranchDto>> updateBranch(
            @PathVariable Long id, @Valid @RequestBody UpdateBranchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(branchService.updateBranch(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Deactivate branch", description = "Soft-deletes by setting isActive = false")
    public ResponseEntity<ApiResponse<Void>> deleteBranch(@PathVariable Long id) {
        branchService.deleteBranch(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
