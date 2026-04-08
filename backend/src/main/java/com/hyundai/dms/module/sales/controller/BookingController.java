package com.hyundai.dms.module.sales.controller;

import com.hyundai.dms.common.ApiResponse;
import com.hyundai.dms.common.PageResponse;
import com.hyundai.dms.common.filter.FilterRequest;
import com.hyundai.dms.module.sales.dto.BookingDto;
import com.hyundai.dms.module.sales.dto.CreateBookingRequest;
import com.hyundai.dms.module.sales.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking Management", description = "Vehicle booking with automatic hold")
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    @Operation(summary = "List bookings", description = "Paginated, filtered booking list")
    public ResponseEntity<ApiResponse<PageResponse<BookingDto>>> listBookings(FilterRequest filterRequest) {
        PageResponse<BookingDto> response = bookingService.listBookings(filterRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/filter")
    @Operation(summary = "Filter bookings", description = "POST-based paginated, filtered booking list")
    public ResponseEntity<ApiResponse<PageResponse<BookingDto>>> filterBookings(
            @RequestBody FilterRequest filterRequest) {
        PageResponse<BookingDto> response = bookingService.listBookings(filterRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking by ID")
    public ResponseEntity<ApiResponse<BookingDto>> getBookingById(@PathVariable Long id) {
        BookingDto booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(ApiResponse.success(booking));
    }

    @PostMapping
    @Operation(summary = "Create booking", description = "Automatically puts the vehicle on HOLD via VehicleService")
    public ResponseEntity<ApiResponse<BookingDto>> createBooking(
            @Valid @RequestBody CreateBookingRequest request) {
        BookingDto booking = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(booking));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel booking", description = "Only ACTIVE bookings can be cancelled")
    public ResponseEntity<ApiResponse<BookingDto>> cancelBooking(@PathVariable Long id) {
        BookingDto booking = bookingService.cancelBooking(id);
        return ResponseEntity.ok(ApiResponse.success(booking));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update booking", description = "Update booking details")
    public ResponseEntity<ApiResponse<BookingDto>> updateBooking(
            @PathVariable Long id, @Valid @RequestBody com.hyundai.dms.module.sales.dto.UpdateBookingRequest request) {
        BookingDto booking = bookingService.updateBooking(id, request);
        return ResponseEntity.ok(ApiResponse.success(booking));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete booking", description = "SUPER_ADMIN only")
    public ResponseEntity<ApiResponse<Void>> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
