package com.hyundai.dms.module.testdrive.service.impl;

import com.hyundai.dms.common.filter.FilterCriteria;
import com.hyundai.dms.common.filter.SpecificationBuilder;
import com.hyundai.dms.exception.ResourceNotFoundException;
import com.hyundai.dms.module.sales.entity.Customer;
import com.hyundai.dms.module.sales.repository.CustomerRepository;
import com.hyundai.dms.module.testdrive.dto.TestDriveBookingDto;
import com.hyundai.dms.module.testdrive.entity.TestDriveBooking;
import com.hyundai.dms.module.testdrive.entity.TestDriveFleet;
import com.hyundai.dms.module.testdrive.mapper.TestDriveBookingMapper;
import com.hyundai.dms.module.testdrive.repository.TestDriveBookingRepository;
import com.hyundai.dms.module.testdrive.repository.TestDriveFleetRepository;
import com.hyundai.dms.module.testdrive.service.TestDriveBookingService;
import com.hyundai.dms.module.user.entity.User;
import com.hyundai.dms.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TestDriveBookingServiceImpl implements TestDriveBookingService {

    private final TestDriveBookingRepository testDriveBookingRepository;
    private final TestDriveFleetRepository testDriveFleetRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final TestDriveBookingMapper testDriveBookingMapper;
    private final SpecificationBuilder<TestDriveBooking> specificationBuilder = new SpecificationBuilder<>();

    @Override
    @Transactional(readOnly = true)
    public com.hyundai.dms.common.PageResponse<TestDriveBookingDto> searchBookings(com.hyundai.dms.common.filter.FilterRequest filterRequest) {
        Specification<TestDriveBooking> spec = specificationBuilder.build(filterRequest.filters());
        
        List<org.springframework.data.domain.Sort.Order> orders = filterRequest.sorts().stream()
                .map(s -> new org.springframework.data.domain.Sort.Order(
                        org.springframework.data.domain.Sort.Direction.fromString(s.direction()), 
                        s.field()))
                .toList();
                
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                filterRequest.page(), 
                filterRequest.size(), 
                org.springframework.data.domain.Sort.by(orders));
                
        return com.hyundai.dms.common.PageUtils.toPageResponse(
                testDriveBookingRepository.findAll(spec, pageable).map(testDriveBookingMapper::toDto)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TestDriveBookingDto getBookingById(Long id) {
        TestDriveBooking entity = testDriveBookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TestDriveBooking", id));
        return testDriveBookingMapper.toDto(entity);
    }

    @Override
    @Transactional
    public TestDriveBookingDto createBooking(TestDriveBookingDto dto) {
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", dto.getCustomerId()));
        TestDriveFleet fleet = testDriveFleetRepository.findById(dto.getFleetId())
                .orElseThrow(() -> new ResourceNotFoundException("TestDriveFleet", dto.getFleetId()));
        User salesExec = null;
        if (dto.getSalesExecutiveId() != null) {
            salesExec = userRepository.findById(dto.getSalesExecutiveId())
                .orElseThrow(() -> new ResourceNotFoundException("User", dto.getSalesExecutiveId()));
        }

        TestDriveBooking entity = testDriveBookingMapper.toEntity(dto);
        entity.setCustomer(customer);
        entity.setFleet(fleet);
        entity.setSalesExecutive(salesExec);

        TestDriveBooking saved = testDriveBookingRepository.save(entity);
        return testDriveBookingMapper.toDto(saved);
    }

    @Override
    @Transactional
    public TestDriveBookingDto updateBooking(Long id, TestDriveBookingDto dto) {
        TestDriveBooking entity = testDriveBookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TestDriveBooking", id));

        if (dto.getCustomerId() != null && !dto.getCustomerId().equals(entity.getCustomer().getId())) {
             Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", dto.getCustomerId()));
             entity.setCustomer(customer);
        }

        if (dto.getFleetId() != null && !dto.getFleetId().equals(entity.getFleet().getId())) {
             TestDriveFleet fleet = testDriveFleetRepository.findById(dto.getFleetId())
                .orElseThrow(() -> new ResourceNotFoundException("TestDriveFleet", dto.getFleetId()));
             entity.setFleet(fleet);
        }

        if (dto.getSalesExecutiveId() != null) {
            if (entity.getSalesExecutive() == null || !dto.getSalesExecutiveId().equals(entity.getSalesExecutive().getId())) {
                User salesExec = userRepository.findById(dto.getSalesExecutiveId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", dto.getSalesExecutiveId()));
                entity.setSalesExecutive(salesExec);
            }
        } else {
            entity.setSalesExecutive(null);
        }

        entity.setBookingId(dto.getBookingId());
        entity.setBookingDate(dto.getBookingDate());
        entity.setTestDriveDate(dto.getTestDriveDate());
        entity.setTimeSlot(dto.getTimeSlot());
        entity.setLicenseNumber(dto.getLicenseNumber());
        entity.setPickupRequired(dto.getPickupRequired());
        
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }

        TestDriveBooking saved = testDriveBookingRepository.save(entity);
        return testDriveBookingMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteBooking(Long id) {
        TestDriveBooking entity = testDriveBookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TestDriveBooking", id));
        testDriveBookingRepository.delete(entity);
    }
}
