package com.hyundai.dms.module.service.service.impl;

import com.hyundai.dms.exception.ResourceNotFoundException;
import com.hyundai.dms.module.service.dto.ServiceRecordDto;
import com.hyundai.dms.module.service.entity.ServiceBooking;
import com.hyundai.dms.module.service.entity.ServiceRecord;
import com.hyundai.dms.module.service.enums.PaymentStatus;
import com.hyundai.dms.module.service.enums.ServiceRecordStatus;
import com.hyundai.dms.module.service.mapper.ServiceRecordMapper;
import com.hyundai.dms.module.service.repository.ServiceBookingRepository;
import com.hyundai.dms.module.service.repository.ServiceRecordRepository;
import com.hyundai.dms.module.service.service.ServiceRecordService;
import com.hyundai.dms.module.user.entity.Branch;
import com.hyundai.dms.module.user.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceRecordServiceImpl implements ServiceRecordService {

    private final ServiceRecordRepository recordRepository;
    private final ServiceBookingRepository bookingRepository;
    private final BranchRepository branchRepository;
    private final ServiceRecordMapper mapper;

    @Override
    @Transactional
    public ServiceRecordDto createRecord(ServiceRecordDto dto) {
        log.info("Creating new service record for booking id: {}", dto.getServiceBookingId());

        if (recordRepository.existsByServiceBookingId(dto.getServiceBookingId())) {
            throw new IllegalArgumentException("Service Record already exists for booking ID: " + dto.getServiceBookingId());
        }

        Branch branch = branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", dto.getBranchId()));
        ServiceBooking booking = bookingRepository.findById(dto.getServiceBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("ServiceBooking", "id", dto.getServiceBookingId()));

        ServiceRecord entity = mapper.toEntity(dto);
        entity.setBranch(branch);
        entity.setServiceBooking(booking);
        
        if (entity.getStatus() == null) {
            entity.setStatus(ServiceRecordStatus.IN_PROGRESS);
        }
        if (entity.getPaymentStatus() == null) {
            entity.setPaymentStatus(PaymentStatus.UNPAID);
        }

        entity = recordRepository.save(entity);
        return mapper.toDto(entity);
    }

    @Override
    @Transactional
    public ServiceRecordDto updateRecord(Long id, ServiceRecordDto dto) {
        log.info("Updating service record id: {}", id);

        ServiceRecord entity = recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceRecord", "id", id));

        if (!entity.getBranch().getId().equals(dto.getBranchId())) {
            Branch branch = branchRepository.findById(dto.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", dto.getBranchId()));
            entity.setBranch(branch);
        }

        if (!entity.getServiceBooking().getId().equals(dto.getServiceBookingId())) {
            ServiceBooking booking = bookingRepository.findById(dto.getServiceBookingId())
                    .orElseThrow(() -> new ResourceNotFoundException("ServiceBooking", "id", dto.getServiceBookingId()));
            entity.setServiceBooking(booking);
        }

        entity.setServiceDate(dto.getServiceDate());
        entity.setOdometer(dto.getOdometer());
        entity.setWorkPerformed(dto.getWorkPerformed());
        entity.setPartsUsed(dto.getPartsUsed());
        entity.setNoOfTechnicians(dto.getNoOfTechnicians());
        entity.setTechnicianHours(dto.getTechnicianHours());
        entity.setNotes(dto.getNotes());
        
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
        if (dto.getPaymentStatus() != null) {
            entity.setPaymentStatus(dto.getPaymentStatus());
        }

        entity = recordRepository.save(entity);
        return mapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceRecordDto getRecordById(Long id) {
        ServiceRecord entity = recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceRecord", "id", id));
        return mapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceRecordDto> getAllRecordsByBranch(Long branchId) {
        List<ServiceRecord> records = recordRepository.findByBranchId(branchId);
        return mapper.toDtoList(records);
    }

    @Override
    @Transactional
    public void deleteRecord(Long id) {
        log.info("Deleting service record id: {}", id);
        ServiceRecord entity = recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceRecord", "id", id));
        recordRepository.delete(entity);
    }
}
