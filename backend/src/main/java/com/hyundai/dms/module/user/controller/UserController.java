package com.hyundai.dms.module.user.controller;

import com.hyundai.dms.common.ApiResponse;
import com.hyundai.dms.common.PageResponse;
import com.hyundai.dms.module.user.dto.*;
import com.hyundai.dms.module.user.service.UserService;
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

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
@Tag(name = "User Management", description = "Admin-only user CRUD operations")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "List users", description = "Paginated list with optional search by username/email")
    public ResponseEntity<ApiResponse<PageResponse<UserDto>>> getAllUsers(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        PageResponse<UserDto> response = userService.getAllUsers(search, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping
    @Operation(summary = "Create user", description = "Create a new user with BCrypt-hashed password")
    public ResponseEntity<ApiResponse<UserDto>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserDto user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update user role, branch, or active status")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        UserDto user = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PatchMapping("/{id}/lock")
    @Operation(summary = "Lock/unlock user")
    public ResponseEntity<ApiResponse<UserDto>> lockUnlockUser(
            @PathVariable Long id, @RequestBody LockRequest request) {
        UserDto user = userService.lockUnlockUser(id, request.isLock());
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PatchMapping("/{id}/reset-password")
    @Operation(summary = "Reset password", description = "Generate temporary password and force change on next login")
    public ResponseEntity<ApiResponse<Map<String, String>>> resetPassword(@PathVariable Long id) {
        String tempPassword = userService.resetPassword(id);
        return ResponseEntity.ok(ApiResponse.success(Map.of("temporaryPassword", tempPassword)));
    }
}
