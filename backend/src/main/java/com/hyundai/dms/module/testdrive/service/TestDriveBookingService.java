package com.hyundai.dms.module.testdrive.service;

import com.hyundai.dms.common.filter.FilterCriteria;
import com.hyundai.dms.module.testdrive.dto.TestDriveBookingDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TestDriveBookingService {
    com.hyundai.dms.common.PageResponse<TestDriveBookingDto> searchBookings(com.hyundai.dms.common.filter.FilterRequest filterRequest);
    TestDriveBookingDto getBookingById(Long id);
    TestDriveBookingDto createBooking(TestDriveBookingDto dto);
    TestDriveBookingDto updateBooking(Long id, TestDriveBookingDto dto);
    void deleteBooking(Long id);
}
