package com.hyundai.dms.module.testdrive.service.impl;

import com.hyundai.dms.common.filter.QueryDslPredicateBuilder;
import com.hyundai.dms.exception.ResourceNotFoundException;
import com.hyundai.dms.module.testdrive.dto.TestDriveFleetDto;
import com.hyundai.dms.module.testdrive.entity.QTestDriveFleet;
import com.hyundai.dms.module.testdrive.entity.TestDriveFleet;
import com.hyundai.dms.module.testdrive.mapper.TestDriveFleetMapper;
import com.hyundai.dms.module.testdrive.repository.TestDriveFleetRepository;
import com.hyundai.dms.module.testdrive.service.TestDriveFleetService;
import com.hyundai.dms.module.user.entity.Branch;
import com.hyundai.dms.module.user.repository.BranchRepository;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import com.hyundai.dms.common.PageResponse;
import com.hyundai.dms.common.filter.FilterRequest;
import com.hyundai.dms.common.filter.FilterCriteria;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TestDriveFleetServiceImpl implements TestDriveFleetService {

    private final TestDriveFleetRepository testDriveFleetRepository;
    private final TestDriveFleetMapper testDriveFleetMapper;
    private final BranchRepository branchRepository;
    private final QueryDslPredicateBuilder<TestDriveFleet> predicateBuilder = new QueryDslPredicateBuilder<>(TestDriveFleet.class);

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TestDriveFleetDto> searchFleet(FilterRequest filterRequest) {
        // Extract globalSearch before passing to generic predicate builder
        String globalSearch = null;
        List<FilterCriteria> remaining = new ArrayList<>();
        if (filterRequest.filters() != null) {
            for (FilterCriteria f : filterRequest.filters()) {
                if ("globalSearch".equals(f.field())) {
                    globalSearch = (String) f.value();
                } else {
                    remaining.add(f);
                }
            }
        }

        BooleanBuilder builder = new BooleanBuilder(predicateBuilder.build(remaining));

        if (globalSearch != null && !globalSearch.isBlank()) {
            QTestDriveFleet q = QTestDriveFleet.testDriveFleet;
            builder.and(
                q.vin.containsIgnoreCase(globalSearch)
                .or(q.brand.containsIgnoreCase(globalSearch))
                .or(q.model.containsIgnoreCase(globalSearch))
                .or(q.variant.containsIgnoreCase(globalSearch))
                .or(q.fleetId.containsIgnoreCase(globalSearch))
                .or(q.registrationNumber.containsIgnoreCase(globalSearch))
            );
        }
        
        List<Sort.Order> orders = filterRequest.sorts().stream()
                .map(s -> new Sort.Order(Sort.Direction.fromString(s.direction()), s.field()))
                .toList();
                
        Pageable pageable = PageRequest.of(filterRequest.page(), filterRequest.size(), Sort.by(orders));
        
        Page<TestDriveFleet> result = testDriveFleetRepository.findAll(builder, pageable);
        return com.hyundai.dms.common.PageUtils.toPageResponse(result.map(testDriveFleetMapper::toDto));
    }

    @Override
    @Transactional(readOnly = true)
    public TestDriveFleetDto getFleetById(Long id) {
        TestDriveFleet entity = testDriveFleetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TestDriveFleet", id));
        return testDriveFleetMapper.toDto(entity);
    }

    @Override
    @Transactional
    public TestDriveFleetDto createFleet(TestDriveFleetDto dto) {
        Branch branch = branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", dto.getBranchId()));

        TestDriveFleet entity = testDriveFleetMapper.toEntity(dto);
        entity.setBranch(branch);
        
        TestDriveFleet saved = testDriveFleetRepository.save(entity);
        return testDriveFleetMapper.toDto(saved);
    }

    @Override
    @Transactional
    public TestDriveFleetDto updateFleet(Long id, TestDriveFleetDto dto) {
        TestDriveFleet entity = testDriveFleetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TestDriveFleet", id));

        if (dto.getBranchId() != null && !dto.getBranchId().equals(entity.getBranch().getId())) {
             Branch branch = branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", dto.getBranchId()));
             entity.setBranch(branch);
        }

        entity.setFleetId(dto.getFleetId());
        entity.setVin(dto.getVin());
        entity.setBrand(dto.getBrand());
        entity.setModel(dto.getModel());
        entity.setVariant(dto.getVariant());
        entity.setFuelType(dto.getFuelType());
        entity.setTransmission(dto.getTransmission());
        entity.setRegistrationNumber(dto.getRegistrationNumber());
        entity.setInsuranceExpiry(dto.getInsuranceExpiry());
        entity.setRcExpiry(dto.getRcExpiry());
        entity.setCurrentOdometer(dto.getCurrentOdometer());
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
        entity.setLastServiceDate(dto.getLastServiceDate());
        entity.setNextServiceDue(dto.getNextServiceDue());

        TestDriveFleet saved = testDriveFleetRepository.save(entity);
        return testDriveFleetMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteFleet(Long id) {
        TestDriveFleet entity = testDriveFleetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TestDriveFleet", id));
        testDriveFleetRepository.delete(entity);
    }
}
