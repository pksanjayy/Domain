package com.hyundai.dms.module.user.controller;

import com.hyundai.dms.common.ApiResponse;
import com.hyundai.dms.module.user.dto.CreateMenuRequest;
import com.hyundai.dms.module.user.dto.UpdateMenuRequest;
import com.hyundai.dms.module.user.service.MenuService;
import com.hyundai.dms.security.dto.MenuDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/menus")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
@Tag(name = "Menu Management", description = "Admin-only menu CRUD operations")
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    @Operation(summary = "Get all menus", description = "Hierarchical menu structure")
    public ResponseEntity<ApiResponse<List<MenuDto>>> getAllMenus() {
        List<MenuDto> tree = menuService.getMenuTree();
        return ResponseEntity.ok(ApiResponse.success(tree));
    }

    @GetMapping("/tree")
    @Operation(summary = "Get menu tree", description = "Hierarchical menu structure")
    public ResponseEntity<ApiResponse<List<MenuDto>>> getMenuTree() {
        List<MenuDto> tree = menuService.getMenuTree();
        return ResponseEntity.ok(ApiResponse.success(tree));
    }

    @PostMapping
    @Operation(summary = "Create menu item")
    public ResponseEntity<ApiResponse<MenuDto>> createMenu(@Valid @RequestBody CreateMenuRequest request) {
        MenuDto menu = menuService.createMenu(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(menu));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update menu item")
    public ResponseEntity<ApiResponse<MenuDto>> updateMenu(
            @PathVariable Long id, @Valid @RequestBody UpdateMenuRequest request) {
        MenuDto menu = menuService.updateMenu(id, request);
        return ResponseEntity.ok(ApiResponse.success(menu));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete menu item")
    public ResponseEntity<ApiResponse<Void>> deleteMenu(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
