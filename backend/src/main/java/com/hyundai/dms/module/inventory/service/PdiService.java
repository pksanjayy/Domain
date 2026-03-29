package com.hyundai.dms.module.inventory.service;

import com.hyundai.dms.audit.Audited;
import com.hyundai.dms.common.enums.ActionType;
import com.hyundai.dms.common.logging.LogExecution;
import com.hyundai.dms.exception.BusinessRuleException;
import com.hyundai.dms.exception.ResourceNotFoundException;
import com.hyundai.dms.module.inventory.dto.PdiChecklistDto;
import com.hyundai.dms.module.inventory.dto.PdiChecklistItemDto;
import com.hyundai.dms.module.inventory.dto.UpdatePdiItemRequest;
import com.hyundai.dms.module.inventory.entity.PdiChecklist;
import com.hyundai.dms.module.inventory.entity.PdiChecklistItem;
import com.hyundai.dms.module.inventory.entity.Vehicle;
import com.hyundai.dms.module.inventory.enums.PdiItemResult;
import com.hyundai.dms.module.inventory.enums.PdiOverallStatus;
import com.hyundai.dms.module.inventory.enums.StockStatus;
import com.hyundai.dms.module.inventory.mapper.PdiMapper;
import com.hyundai.dms.module.inventory.repository.PdiChecklistItemRepository;
import com.hyundai.dms.module.inventory.repository.PdiChecklistRepository;
import com.hyundai.dms.module.inventory.repository.VehicleRepository;
import com.hyundai.dms.module.inventory.validator.StockStatusTransitionValidator;
import com.hyundai.dms.module.user.entity.User;
import com.hyundai.dms.module.user.repository.UserRepository;
import com.hyundai.dms.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdiService {

    private final PdiChecklistRepository pdiChecklistRepository;
    private final PdiChecklistItemRepository pdiChecklistItemRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final PdiMapper pdiMapper;
    private final StockStatusTransitionValidator transitionValidator;

    /**
     * Default PDI inspection points for every new checklist.
     */
    private static final List<String> DEFAULT_PDI_POINTS = Arrays.asList(
            "Exterior Body Inspection",
            "Paint Quality Check",
            "Interior Upholstery Check",
            "Engine Bay Inspection",
            "Brake System Test",
            "Tyre Condition & Pressure",
            "Electrical Systems Test",
            "AC & Climate Control",
            "Infotainment System",
            "Fluid Levels Check"
    );

    @LogExecution
    @Audited(entity = "PdiChecklist", action = ActionType.CREATE)
    @PreAuthorize("hasRole('ROLE_WORKSHOP_EXEC') or hasRole('ROLE_SUPER_ADMIN')")
    @Transactional
    public PdiChecklistDto createChecklist(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", vehicleId));

        if (pdiChecklistRepository.existsByVehicleId(vehicleId)) {
            throw new BusinessRuleException("PDI checklist already exists for vehicle VIN=" + vehicle.getVin());
        }

        // Transition: GRN_RECEIVED → PDI_PENDING
        transitionValidator.validate(vehicle.getStatus(), StockStatus.PDI_PENDING);

        PdiChecklist checklist = PdiChecklist.builder()
                .vehicle(vehicle)
                .overallStatus(PdiOverallStatus.PENDING)
                .build();

        // Create default checklist items
        for (String point : DEFAULT_PDI_POINTS) {
            PdiChecklistItem item = PdiChecklistItem.builder()
                    .checklist(checklist)
                    .pointName(point)
                    .result(PdiItemResult.NA)
                    .build();
            checklist.getItems().add(item);
        }

        checklist = pdiChecklistRepository.save(checklist);

        // Transition vehicle status
        vehicle.setStatus(StockStatus.PDI_PENDING);
        vehicleRepository.save(vehicle);

        log.info("PDI checklist created for vehicle VIN={}", vehicle.getVin());
        return pdiMapper.toDto(checklist);
    }

    @LogExecution
    @Audited(entity = "PdiChecklistItem", action = ActionType.UPDATE)
    @PreAuthorize("hasRole('ROLE_WORKSHOP_EXEC') or hasRole('ROLE_SUPER_ADMIN')")
    @Transactional
    public PdiChecklistItemDto updateChecklistItem(Long checklistId, Long itemId, UpdatePdiItemRequest request) {
        PdiChecklist checklist = pdiChecklistRepository.findById(checklistId)
                .orElseThrow(() -> new ResourceNotFoundException("PdiChecklist", checklistId));

        if (checklist.getOverallStatus() != PdiOverallStatus.PENDING) {
            throw new BusinessRuleException("Cannot update items on a completed checklist");
        }

        PdiChecklistItem item = pdiChecklistItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("PdiChecklistItem", itemId));

        if (!item.getChecklist().getId().equals(checklistId)) {
            throw new BusinessRuleException("Item does not belong to the specified checklist");
        }

        item.setResult(PdiItemResult.valueOf(request.getResult().toUpperCase()));
        item.setPhotoUrl(request.getPhotoUrl());
        item.setRemark(request.getRemark());

        item = pdiChecklistItemRepository.save(item);
        log.info("Updated PDI item {} result={}", item.getPointName(), item.getResult());
        return pdiMapper.toItemDto(item);
    }

    @LogExecution
    @Audited(entity = "PdiChecklist", action = ActionType.UPDATE)
    @PreAuthorize("hasRole('ROLE_WORKSHOP_EXEC') or hasRole('ROLE_SUPER_ADMIN')")
    @Transactional
    public PdiChecklistDto completeChecklist(Long checklistId) {
        PdiChecklist checklist = pdiChecklistRepository.findById(checklistId)
                .orElseThrow(() -> new ResourceNotFoundException("PdiChecklist", checklistId));

        if (checklist.getOverallStatus() != PdiOverallStatus.PENDING) {
            throw new BusinessRuleException("Checklist is already completed");
        }

        List<PdiChecklistItem> items = checklist.getItems();
        if (items.isEmpty()) {
            throw new BusinessRuleException("Checklist has no items to evaluate");
        }

        // Check if any items still have NA result
        boolean hasUnreviewed = items.stream()
                .anyMatch(item -> item.getResult() == PdiItemResult.NA);
        if (hasUnreviewed) {
            throw new BusinessRuleException("All checklist items must be reviewed before completion");
        }

        // Determine overall status
        boolean allPassed = items.stream()
                .allMatch(item -> item.getResult() == PdiItemResult.PASS);

        User currentUser = getCurrentUser();

        checklist.setOverallStatus(allPassed ? PdiOverallStatus.PASSED : PdiOverallStatus.FAILED);
        checklist.setCompletedBy(currentUser);
        checklist.setCompletedAt(LocalDateTime.now());

        checklist = pdiChecklistRepository.save(checklist);

        // If all items passed, auto-transition to PDI_DONE then AVAILABLE
        if (allPassed) {
            Vehicle vehicle = checklist.getVehicle();
            transitionValidator.validate(vehicle.getStatus(), StockStatus.PDI_DONE);
            vehicle.setStatus(StockStatus.PDI_DONE);
            vehicleRepository.save(vehicle);
            log.info("PDI PASSED for vehicle VIN={} — status set to PDI_DONE", vehicle.getVin());
        } else {
            log.info("PDI FAILED for vehicle VIN={}", checklist.getVehicle().getVin());
        }

        return pdiMapper.toDto(checklist);
    }

    @LogExecution
    @Transactional(readOnly = true)
    public PdiChecklistDto getChecklistByVehicleId(Long vehicleId) {
        PdiChecklist checklist = pdiChecklistRepository.findByVehicleId(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("PdiChecklist", "vehicleId", vehicleId));
        return pdiMapper.toDto(checklist);
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userRepository.findById(userDetails.getId()).orElse(null);
        }
        return null;
    }
}
