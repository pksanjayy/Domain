package com.hyundai.dms.module.service.service;

import com.hyundai.dms.module.service.dto.ServiceBookingDto;

import java.util.List;

public interface ServiceBookingService {
    ServiceBookingDto createBooking(ServiceBookingDto dto);
    ServiceBookingDto updateBooking(Long id, ServiceBookingDto dto);
    ServiceBookingDto getBookingById(Long id);
    List<ServiceBookingDto> getAllBookingsByBranch(Long branchId);
    void deleteBooking(Long id);
}
