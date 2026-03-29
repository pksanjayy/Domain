package com.hyundai.dms.module.user.controller;

import com.hyundai.dms.common.ApiResponse;
import com.hyundai.dms.module.user.dto.RoleDto;
import com.hyundai.dms.module.user.service.RoleService;
import com.hyundai.dms.security.dto.PermissionDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
@Tag(name = "Role Management", description = "Admin-only role configuration")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @Operation(summary = "List all roles", description = "Returns all roles with permissions and menus")
    public ResponseEntity<ApiResponse<List<RoleDto>>> getAllRoles() {
        List<RoleDto> roles = roleService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    @PutMapping("/{id}/permissions")
    @Operation(summary = "Update role permissions", description = "Replace the permission matrix for a role")
    public ResponseEntity<ApiResponse<RoleDto>> updatePermissions(
            @PathVariable Long id, @RequestBody List<Map<String, Object>> frontendPermissions) {

        // Convert frontend {module, action} pairs to backend PermissionDto {moduleName, canCreate/Read/Update/Delete}
        Map<String, PermissionDto> grouped = new HashMap<>();
        for (Map<String, Object> entry : frontendPermissions) {
            String module = (String) entry.get("module");
            String action = (String) entry.get("action");
            if (module == null) {
                // Fallback: maybe it's already in backend format (moduleName)
                module = (String) entry.get("moduleName");
            }
            if (module == null) continue;

            PermissionDto dto = grouped.computeIfAbsent(module, m -> {
                PermissionDto p = new PermissionDto();
                p.setModuleName(m);
                return p;
            });

            if (action != null) {
                switch (action.toUpperCase()) {
                    case "CREATE" -> dto.setCanCreate(true);
                    case "READ" -> dto.setCanRead(true);
                    case "UPDATE" -> dto.setCanUpdate(true);
                    case "DELETE" -> dto.setCanDelete(true);
                }
            } else {
                // Already in backend format with canCreate/canRead/etc
                if (Boolean.TRUE.equals(entry.get("canCreate"))) dto.setCanCreate(true);
                if (Boolean.TRUE.equals(entry.get("canRead"))) dto.setCanRead(true);
                if (Boolean.TRUE.equals(entry.get("canUpdate"))) dto.setCanUpdate(true);
                if (Boolean.TRUE.equals(entry.get("canDelete"))) dto.setCanDelete(true);
            }
        }

        List<PermissionDto> permissions = new ArrayList<>(grouped.values());
        RoleDto role = roleService.updatePermissions(id, permissions);
        return ResponseEntity.ok(ApiResponse.success(role));
    }

    @PutMapping("/{id}/menus")
    @Operation(summary = "Update role menus", description = "Replace the accessible menus for a role")
    public ResponseEntity<ApiResponse<RoleDto>> updateMenus(
            @PathVariable Long id, @RequestBody List<Long> menuIds) {
        RoleDto role = roleService.updateMenus(id, menuIds);
        return ResponseEntity.ok(ApiResponse.success(role));
    }
}
