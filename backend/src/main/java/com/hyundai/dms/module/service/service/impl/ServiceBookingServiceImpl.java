package com.hyundai.dms.module.service.service.impl;

import com.hyundai.dms.exception.ResourceNotFoundException;
import com.hyundai.dms.module.sales.entity.Customer;
import com.hyundai.dms.module.sales.repository.CustomerRepository;
import com.hyundai.dms.module.service.dto.ServiceBookingDto;
import com.hyundai.dms.module.service.entity.ServiceBooking;
import com.hyundai.dms.module.service.enums.ServiceBookingStatus;
import com.hyundai.dms.module.service.mapper.ServiceBookingMapper;
import com.hyundai.dms.module.service.repository.ServiceBookingRepository;
import com.hyundai.dms.module.service.service.ServiceBookingService;
import com.hyundai.dms.module.user.entity.Branch;
import com.hyundai.dms.module.user.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceBookingServiceImpl implements ServiceBookingService {

    private final ServiceBookingRepository bookingRepository;
    private final BranchRepository branchRepository;
    private final CustomerRepository customerRepository;
    private final ServiceBookingMapper mapper;

    @Override
    @Transactional
    public ServiceBookingDto createBooking(ServiceBookingDto dto) {
        log.info("Creating new service booking for customer id: {}", dto.getCustomerId());

        Branch branch = branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", dto.getBranchId()));
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", dto.getCustomerId()));

        ServiceBooking entity = mapper.toEntity(dto);
        entity.setBranch(branch);
        entity.setCustomer(customer);

        String uniqueBookingId = "BKG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        entity.setBookingId(uniqueBookingId);
        
        if (entity.getStatus() == null) {
            entity.setStatus(ServiceBookingStatus.CONFIRMED);
        }

        entity = bookingRepository.save(entity);
        return mapper.toDto(entity);
    }

    @Override
    @Transactional
    public ServiceBookingDto updateBooking(Long id, ServiceBookingDto dto) {
        log.info("Updating service booking id: {}", id);

        ServiceBooking entity = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceBooking", "id", id));

        if (!entity.getBranch().getId().equals(dto.getBranchId())) {
            Branch branch = branchRepository.findById(dto.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", dto.getBranchId()));
            entity.setBranch(branch);
        }

        if (!entity.getCustomer().getId().equals(dto.getCustomerId())) {
            Customer customer = customerRepository.findById(dto.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", dto.getCustomerId()));
            entity.setCustomer(customer);
        }

        entity.setBookingDate(dto.getBookingDate());
        entity.setPreferredServiceDate(dto.getPreferredServiceDate());
        entity.setServiceType(dto.getServiceType());
        entity.setComplaints(dto.getComplaints());
        
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }

        entity = bookingRepository.save(entity);
        return mapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceBookingDto getBookingById(Long id) {
        ServiceBooking entity = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceBooking", "id", id));
        return mapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceBookingDto> getAllBookingsByBranch(Long branchId) {
        List<ServiceBooking> bookings = bookingRepository.findByBranchId(branchId);
        return mapper.toDtoList(bookings);
    }

    @Override
    @Transactional
    public void deleteBooking(Long id) {
        log.info("Deleting service booking id: {}", id);
        ServiceBooking entity = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceBooking", "id", id));
        bookingRepository.delete(entity);
    }
}
