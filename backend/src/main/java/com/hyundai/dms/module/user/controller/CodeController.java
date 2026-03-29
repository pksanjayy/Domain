package com.hyundai.dms.module.user.controller;

import com.hyundai.dms.common.ApiResponse;
import com.hyundai.dms.common.PageResponse;
import com.hyundai.dms.module.user.dto.CodeDto;
import com.hyundai.dms.module.user.service.CodeService;
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
@RequestMapping("/api/admin/codes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
@Tag(name = "Code Management", description = "Generic lookup table CRUD")
public class CodeController {

    private final CodeService codeService;

    @GetMapping
    @Operation(summary = "List all codes", description = "Paginated list of all codes")
    public ResponseEntity<ApiResponse<PageResponse<CodeDto>>> getAllCodes(
            @PageableDefault(size = 20, sort = "category", direction = Sort.Direction.ASC) Pageable pageable) {
        PageResponse<CodeDto> codes = codeService.getAllCodes(pageable);
        return ResponseEntity.ok(ApiResponse.success(codes));
    }

    @GetMapping("/by-category/{category}")
    @Operation(summary = "List codes by category", description = "Returns cached codes for a given category")
    public ResponseEntity<ApiResponse<List<CodeDto>>> getByCategory(@PathVariable String category) {
        List<CodeDto> codes = codeService.getByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(codes));
    }

    @PostMapping
    @Operation(summary = "Create code")
    public ResponseEntity<ApiResponse<CodeDto>> createCode(@Valid @RequestBody CodeDto request) {
        CodeDto code = codeService.createCode(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(code));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update code")
    public ResponseEntity<ApiResponse<CodeDto>> updateCode(
            @PathVariable Long id, @Valid @RequestBody CodeDto request) {
        CodeDto code = codeService.updateCode(id, request);
        return ResponseEntity.ok(ApiResponse.success(code));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete code")
    public ResponseEntity<ApiResponse<Void>> deleteCode(@PathVariable Long id) {
        codeService.deleteCode(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
