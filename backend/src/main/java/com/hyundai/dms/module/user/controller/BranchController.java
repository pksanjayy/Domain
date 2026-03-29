package com.hyundai.dms.module.user.controller;

import com.hyundai.dms.common.ApiResponse;
import com.hyundai.dms.exception.ResourceNotFoundException;
import com.hyundai.dms.module.user.dto.BranchDto;
import com.hyundai.dms.module.user.dto.CreateBranchRequest;
import com.hyundai.dms.module.user.dto.UpdateBranchRequest;
import com.hyundai.dms.module.user.entity.Branch;
import com.hyundai.dms.module.user.repository.BranchRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/branches")
@RequiredArgsConstructor
@Tag(name = "Branch Management", description = "Branch CRUD endpoints")
public class BranchController {

    private final BranchRepository branchRepository;

    @GetMapping
    @Operation(summary = "List all branches", description = "Returns all branches for dropdowns and filters")
    public ResponseEntity<ApiResponse<List<BranchDto>>> getAllBranches() {
        List<BranchDto> branches = branchRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(branches));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get branch by ID")
    public ResponseEntity<ApiResponse<BranchDto>> getBranchById(@PathVariable Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", id));
        return ResponseEntity.ok(ApiResponse.success(toDto(branch)));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Create branch")
    public ResponseEntity<ApiResponse<BranchDto>> createBranch(@Valid @RequestBody CreateBranchRequest request) {
        Branch branch = Branch.builder()
                .code(request.getCode())
                .name(request.getName())
                .region(request.getRegion())
                .gstin(request.getGstin())
                .isActive(true)
                .build();
        branch = branchRepository.save(branch);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toDto(branch)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Update branch")
    public ResponseEntity<ApiResponse<BranchDto>> updateBranch(
            @PathVariable Long id, @Valid @RequestBody UpdateBranchRequest request) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", id));
        branch.setName(request.getName());
        branch.setRegion(request.getRegion());
        branch.setGstin(request.getGstin());
        if (request.getIsActive() != null) {
            branch.setIsActive(request.getIsActive());
        }
        branch = branchRepository.save(branch);
        return ResponseEntity.ok(ApiResponse.success(toDto(branch)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Deactivate branch", description = "Soft-deletes by setting isActive = false")
    public ResponseEntity<ApiResponse<Void>> deleteBranch(@PathVariable Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", id));
        branch.setIsActive(false);
        branchRepository.save(branch);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private BranchDto toDto(Branch branch) {
        return BranchDto.builder()
                .id(branch.getId())
                .name(branch.getName())
                .code(branch.getCode())
                .region(branch.getRegion())
                .gstin(branch.getGstin())
                .isActive(Boolean.TRUE.equals(branch.getIsActive()))
                .build();
    }
}
