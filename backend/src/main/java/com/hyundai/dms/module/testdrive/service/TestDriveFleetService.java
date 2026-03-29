package com.hyundai.dms.module.testdrive.service;

import com.hyundai.dms.common.filter.FilterCriteria;
import com.hyundai.dms.module.testdrive.dto.TestDriveFleetDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TestDriveFleetService {
    com.hyundai.dms.common.PageResponse<TestDriveFleetDto> searchFleet(com.hyundai.dms.common.filter.FilterRequest filterRequest);
    TestDriveFleetDto getFleetById(Long id);
    TestDriveFleetDto createFleet(TestDriveFleetDto dto);
    TestDriveFleetDto updateFleet(Long id, TestDriveFleetDto dto);
    void deleteFleet(Long id);
}
