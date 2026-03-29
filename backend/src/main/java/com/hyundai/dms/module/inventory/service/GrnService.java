package com.hyundai.dms.module.inventory.service;

import com.hyundai.dms.audit.Audited;
import com.hyundai.dms.common.PageResponse;
import com.hyundai.dms.common.PageUtils;
import com.hyundai.dms.common.enums.ActionType;
import com.hyundai.dms.common.filter.FilterRequest;
import com.hyundai.dms.common.filter.SpecificationBuilder;
import com.hyundai.dms.common.logging.LogExecution;
import com.hyundai.dms.exception.ResourceNotFoundException;
import com.hyundai.dms.module.inventory.dto.CreateGrnRequest;
import com.hyundai.dms.module.inventory.dto.UpdateGrnRequest;
import com.hyundai.dms.module.inventory.dto.GrnDto;
import com.hyundai.dms.module.inventory.entity.GrnRecord;
import com.hyundai.dms.module.inventory.entity.Vehicle;
import com.hyundai.dms.module.inventory.enums.ArrivalCondition;
import com.hyundai.dms.module.inventory.enums.StockStatus;
import com.hyundai.dms.module.inventory.mapper.GrnMapper;
import com.hyundai.dms.module.inventory.repository.GrnRecordRepository;
import com.hyundai.dms.module.inventory.repository.VehicleRepository;
import com.hyundai.dms.module.inventory.validator.StockStatusTransitionValidator;
import com.hyundai.dms.module.notification.dto.NotificationDto;
import com.hyundai.dms.module.notification.service.NotificationService;
import com.hyundai.dms.common.enums.RoleName;
import com.hyundai.dms.module.user.entity.User;
import com.hyundai.dms.module.user.repository.UserRepository;
import com.hyundai.dms.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrnService {

    private final GrnRecordRepository grnRecordRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final GrnMapper grnMapper;
    private final StockStatusTransitionValidator transitionValidator;
    private final NotificationService notificationService;
    private final SpecificationBuilder<GrnRecord> specificationBuilder = new SpecificationBuilder<>();

    @LogExecution
    @Transactional(readOnly = true)
    public PageResponse<GrnDto> listGrns(FilterRequest filterRequest) {
        Specification<GrnRecord> spec = specificationBuilder.build(filterRequest.filters());
        PageRequest pageRequest = PageUtils.buildPageRequest(
                filterRequest.page(), filterRequest.size(), filterRequest.sorts());
        Page<GrnRecord> page = grnRecordRepository.findAll(spec, pageRequest);
        Page<GrnDto> dtoPage = page.map(grnMapper::toDto);
        return PageUtils.toPageResponse(dtoPage);
    }

    @LogExecution
    @Audited(entity = "GrnRecord", action = ActionType.CREATE)
    @PreAuthorize("hasRole('WORKSHOP_EXEC') or hasRole('SUPER_ADMIN')")
    @Transactional
    public GrnDto receiveGrn(CreateGrnRequest request) {
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", request.getVehicleId()));

        // Validate status transition: must be IN_TRANSIT → GRN_RECEIVED
        transitionValidator.validate(vehicle.getStatus(), StockStatus.GRN_RECEIVED);

        // Generate GRN number
        String grnNumber = "GRN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        if (grnRecordRepository.existsByGrnNumber(grnNumber)) {
            grnNumber = "GRN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        User currentUser = getCurrentUser();

        GrnRecord grn = GrnRecord.builder()
                .grnNumber(grnNumber)
                .vehicle(vehicle)
                .transporterName(request.getTransporterName())
                .dispatchDate(request.getDispatchDate())
                .receivedDate(request.getReceivedDate() != null ? request.getReceivedDate() : LocalDate.now())
                .conditionOnArrival(ArrivalCondition.valueOf(request.getConditionOnArrival().toUpperCase()))
                .remarks(request.getRemarks())
                .receivedBy(currentUser)
                .branch(vehicle.getBranch())
                .build();

        grn = grnRecordRepository.save(grn);

        // Transition vehicle status
        vehicle.setStatus(StockStatus.GRN_RECEIVED);
        vehicleRepository.save(vehicle);

        log.info("GRN {} created for vehicle VIN={}", grnNumber, vehicle.getVin());

        // Notify MANAGER_VIEWER about GRN received
        notificationService.sendToRole(RoleName.MANAGER_VIEWER, NotificationDto.builder()
                .title("GRN Received")
                .message("GRN " + grnNumber + " received for vehicle VIN=" + vehicle.getVin())
                .module("INVENTORY")
                .priority("HIGH")
                .deepLink("/inventory/vehicles/" + vehicle.getId())
                .build(), 
                currentUser != null ? currentUser.getId() : null);

        return grnMapper.toDto(grn);
    }

    @LogExecution
    @Audited(entity = "GrnRecord", action = ActionType.UPDATE)
    @PreAuthorize("hasRole('WORKSHOP_EXEC') or hasRole('SUPER_ADMIN')")
    @Transactional
    public GrnDto updateCondition(Long grnId, String condition, String remarks) {
        GrnRecord grn = grnRecordRepository.findById(grnId)
                .orElseThrow(() -> new ResourceNotFoundException("GrnRecord", grnId));

        grn.setConditionOnArrival(ArrivalCondition.valueOf(condition.toUpperCase()));
        if (remarks != null) {
            grn.setRemarks(remarks);
        }

        grn = grnRecordRepository.save(grn);
        log.info("Updated GRN {} condition to {}", grn.getGrnNumber(), condition);
        return grnMapper.toDto(grn);
    }

    @LogExecution
    @Audited(entity = "GrnRecord", action = ActionType.UPDATE)
    @PreAuthorize("hasRole('WORKSHOP_EXEC') or hasRole('SUPER_ADMIN')")
    @Transactional
    public GrnDto updateGrn(Long grnId, UpdateGrnRequest request) {
        GrnRecord grn = grnRecordRepository.findById(grnId)
                .orElseThrow(() -> new ResourceNotFoundException("GrnRecord", grnId));

        grn.setTransporterName(request.getTransporterName());
        grn.setDispatchDate(request.getDispatchDate());
        if (request.getReceivedDate() != null) {
            grn.setReceivedDate(request.getReceivedDate());
        }
        if (request.getConditionOnArrival() != null) {
            grn.setConditionOnArrival(ArrivalCondition.valueOf(request.getConditionOnArrival().toUpperCase()));
        }
        grn.setRemarks(request.getRemarks());

        grn = grnRecordRepository.save(grn);
        log.info("Updated GRN {}", grn.getGrnNumber());
        return grnMapper.toDto(grn);
    }

    @LogExecution
    @Audited(entity = "GrnRecord", action = ActionType.DELETE)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Transactional
    public void deleteGrn(Long grnId) {
        GrnRecord grn = grnRecordRepository.findById(grnId)
                .orElseThrow(() -> new ResourceNotFoundException("GrnRecord", grnId));

        Vehicle vehicle = grn.getVehicle();
        
        if (vehicle.getStatus() == StockStatus.GRN_RECEIVED) {
            vehicle.setStatus(StockStatus.IN_TRANSIT);
            vehicleRepository.save(vehicle);
        }

        grnRecordRepository.delete(grn);
        log.info("Deleted GRN {}", grn.getGrnNumber());
    }

    @LogExecution
    @Transactional(readOnly = true)
    public GrnDto getGrnById(Long id) {
        GrnRecord grn = grnRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GrnRecord", id));
        return grnMapper.toDto(grn);
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userRepository.findById(userDetails.getId())
                    .orElse(null);
        }
        return null;
    }
}

