package com.hyundai.dms.module.service.controller;

import com.hyundai.dms.module.service.dto.ServiceRecordDto;
import com.hyundai.dms.module.service.service.ServiceRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service/records")
@RequiredArgsConstructor
public class ServiceRecordController {

    private final ServiceRecordService recordService;

    @PostMapping
    public ResponseEntity<ServiceRecordDto> createRecord(@RequestBody ServiceRecordDto dto) {
        ServiceRecordDto created = recordService.createRecord(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceRecordDto> updateRecord(@PathVariable Long id, @RequestBody ServiceRecordDto dto) {
        ServiceRecordDto updated = recordService.updateRecord(id, dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceRecordDto> getRecordById(@PathVariable Long id) {
        ServiceRecordDto record = recordService.getRecordById(id);
        return ResponseEntity.ok(record);
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<ServiceRecordDto>> getAllRecordsByBranch(@PathVariable Long branchId) {
        List<ServiceRecordDto> records = recordService.getAllRecordsByBranch(branchId);
        return ResponseEntity.ok(records);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id) {
        recordService.deleteRecord(id);
        return ResponseEntity.noContent().build();
    }
}
