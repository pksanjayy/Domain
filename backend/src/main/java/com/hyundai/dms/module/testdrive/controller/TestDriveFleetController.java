package com.hyundai.dms.module.testdrive.controller;

import com.hyundai.dms.common.filter.FilterCriteria;
import com.hyundai.dms.module.testdrive.dto.TestDriveFleetDto;
import com.hyundai.dms.module.testdrive.service.TestDriveFleetService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/testdrive/fleet")
@RequiredArgsConstructor
public class TestDriveFleetController {

    private final TestDriveFleetService testDriveFleetService;

    @PostMapping("/filter")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('TEST_DRIVE_READ')")
    public ResponseEntity<com.hyundai.dms.common.ApiResponse<com.hyundai.dms.common.PageResponse<TestDriveFleetDto>>> searchFleet(
            @RequestBody com.hyundai.dms.common.filter.FilterRequest filterRequest) {
        return ResponseEntity.ok(com.hyundai.dms.common.ApiResponse.success(testDriveFleetService.searchFleet(filterRequest)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('TEST_DRIVE_READ')")
    public ResponseEntity<TestDriveFleetDto> getFleet(@PathVariable Long id) {
        return ResponseEntity.ok(testDriveFleetService.getFleetById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('TEST_DRIVE_CREATE')")
    public ResponseEntity<TestDriveFleetDto> createFleet(@RequestBody TestDriveFleetDto dto) {
        return ResponseEntity.ok(testDriveFleetService.createFleet(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('TEST_DRIVE_UPDATE')")
    public ResponseEntity<TestDriveFleetDto> updateFleet(@PathVariable Long id, @RequestBody TestDriveFleetDto dto) {
        return ResponseEntity.ok(testDriveFleetService.updateFleet(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('TEST_DRIVE_DELETE')")
    public ResponseEntity<Void> deleteFleet(@PathVariable Long id) {
        testDriveFleetService.deleteFleet(id);
        return ResponseEntity.ok().build();
    }
}
