package com.hyundai.dms.module.service.service;

import com.hyundai.dms.module.service.dto.ServiceRecordDto;

import java.util.List;

public interface ServiceRecordService {
    ServiceRecordDto createRecord(ServiceRecordDto dto);
    ServiceRecordDto updateRecord(Long id, ServiceRecordDto dto);
    ServiceRecordDto getRecordById(Long id);
    List<ServiceRecordDto> getAllRecordsByBranch(Long branchId);
    void deleteRecord(Long id);
}
