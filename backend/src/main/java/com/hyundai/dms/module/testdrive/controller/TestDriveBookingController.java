package com.hyundai.dms.module.testdrive.controller;

import com.hyundai.dms.common.filter.FilterCriteria;
import com.hyundai.dms.module.testdrive.dto.TestDriveBookingDto;
import com.hyundai.dms.module.testdrive.service.TestDriveBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/testdrive/bookings")
@RequiredArgsConstructor
public class TestDriveBookingController {

    private final TestDriveBookingService testDriveBookingService;

    @PostMapping("/filter")
    @PreAuthorize("hasAuthority('TEST_DRIVE_READ')")
    public ResponseEntity<com.hyundai.dms.common.ApiResponse<com.hyundai.dms.common.PageResponse<TestDriveBookingDto>>> searchBookings(
            @RequestBody com.hyundai.dms.common.filter.FilterRequest filterRequest) {
        return ResponseEntity.ok(com.hyundai.dms.common.ApiResponse.success(testDriveBookingService.searchBookings(filterRequest)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TEST_DRIVE_READ')")
    public ResponseEntity<TestDriveBookingDto> getBooking(@PathVariable Long id) {
        return ResponseEntity.ok(testDriveBookingService.getBookingById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('TEST_DRIVE_CREATE')")
    public ResponseEntity<TestDriveBookingDto> createBooking(@RequestBody TestDriveBookingDto dto) {
        return ResponseEntity.ok(testDriveBookingService.createBooking(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('TEST_DRIVE_UPDATE')")
    public ResponseEntity<TestDriveBookingDto> updateBooking(@PathVariable Long id, @RequestBody TestDriveBookingDto dto) {
        return ResponseEntity.ok(testDriveBookingService.updateBooking(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('TEST_DRIVE_DELETE')")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        testDriveBookingService.deleteBooking(id);
        return ResponseEntity.ok().build();
    }
}
