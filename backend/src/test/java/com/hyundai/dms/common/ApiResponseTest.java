package com.hyundai.dms.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    // ── success ──

    @Test
    @DisplayName("success() creates response with success=true and data")
    void success_basic() {
        ApiResponse<String> response = ApiResponse.success("Hello");

        assertTrue(response.isSuccess());
        assertEquals("Hello", response.getData());
        assertNull(response.getError());
        assertNotNull(response.getTimestamp());
    }

    @Test
    @DisplayName("success() with null data")
    void success_nullData() {
        ApiResponse<Object> response = ApiResponse.success(null);

        assertTrue(response.isSuccess());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("success() with complex object")
    void success_complexObject() {
        record UserDto(Long id, String name) {}
        UserDto user = new UserDto(1L, "admin");
        ApiResponse<UserDto> response = ApiResponse.success(user);

        assertTrue(response.isSuccess());
        assertEquals(1L, response.getData().id());
        assertEquals("admin", response.getData().name());
    }

    @Test
    @DisplayName("success() timestamp is recent")
    void success_timestamp() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        ApiResponse<String> response = ApiResponse.success("test");
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        assertTrue(response.getTimestamp().isAfter(before));
        assertTrue(response.getTimestamp().isBefore(after));
    }

    // ── error ──

    @Test
    @DisplayName("error() creates response with success=false and error details")
    void error_basic() {
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .status(400)
                .errorCode("VALIDATION_ERROR")
                .message("Field is required")
                .path("/api/vehicles")
                .build();

        ApiResponse<Void> response = ApiResponse.error(errorResponse);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertNotNull(response.getError());
        assertEquals(400, response.getError().getStatus());
        assertEquals("VALIDATION_ERROR", response.getError().getErrorCode());
    }

    @Test
    @DisplayName("error() with 500 status")
    void error_serverError() {
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .status(500)
                .errorCode("INTERNAL_ERROR")
                .message("Something went wrong")
                .build();

        ApiResponse<Void> response = ApiResponse.error(errorResponse);

        assertFalse(response.isSuccess());
        assertEquals(500, response.getError().getStatus());
    }

    // ── Builder ──

    @Test
    @DisplayName("Builder sets default timestamp")
    void builder_defaultTimestamp() {
        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true)
                .data("test")
                .build();

        assertNotNull(response.getTimestamp());
    }

    @Test
    @DisplayName("Builder allows custom timestamp override")
    void builder_customTimestamp() {
        LocalDateTime custom = LocalDateTime.of(2026, 1, 1, 0, 0);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true)
                .data("test")
                .timestamp(custom)
                .build();

        assertEquals(custom, response.getTimestamp());
    }
}
