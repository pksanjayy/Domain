package com.hyundai.dms.module.service.controller;

import com.hyundai.dms.module.service.dto.ServiceBookingDto;
import com.hyundai.dms.module.service.service.ServiceBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service/bookings")
@RequiredArgsConstructor
public class ServiceBookingController {

    private final ServiceBookingService bookingService;

    @PostMapping
    public ResponseEntity<ServiceBookingDto> createBooking(@RequestBody ServiceBookingDto dto) {
        ServiceBookingDto created = bookingService.createBooking(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceBookingDto> updateBooking(@PathVariable Long id, @RequestBody ServiceBookingDto dto) {
        ServiceBookingDto updated = bookingService.updateBooking(id, dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceBookingDto> getBookingById(@PathVariable Long id) {
        ServiceBookingDto booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<ServiceBookingDto>> getAllBookingsByBranch(@PathVariable Long branchId) {
        List<ServiceBookingDto> bookings = bookingService.getAllBookingsByBranch(branchId);
        return ResponseEntity.ok(bookings);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }
}
