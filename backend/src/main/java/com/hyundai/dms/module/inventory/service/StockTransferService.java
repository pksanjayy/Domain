package com.hyundai.dms.module.inventory.service;

import com.hyundai.dms.audit.Audited;
import com.hyundai.dms.common.PageResponse;
import com.hyundai.dms.common.PageUtils;
import com.hyundai.dms.common.enums.ActionType;
import com.hyundai.dms.common.filter.FilterRequest;
import com.hyundai.dms.common.filter.QueryDslPredicateBuilder;
import com.hyundai.dms.common.logging.LogExecution;
import com.hyundai.dms.exception.BusinessRuleException;
import com.hyundai.dms.exception.ResourceNotFoundException;
import com.hyundai.dms.module.inventory.dto.RequestTransferRequest;
import com.hyundai.dms.module.inventory.dto.StockTransferDto;
import com.hyundai.dms.module.inventory.entity.StockTransfer;
import com.hyundai.dms.module.inventory.entity.Vehicle;
import com.hyundai.dms.module.inventory.enums.StockStatus;
import com.hyundai.dms.module.inventory.enums.TransferStatus;
import com.hyundai.dms.module.inventory.mapper.StockTransferMapper;
import com.hyundai.dms.module.inventory.repository.StockTransferRepository;
import com.hyundai.dms.module.inventory.repository.VehicleRepository;
import com.hyundai.dms.module.inventory.validator.StockStatusTransitionValidator;
import com.hyundai.dms.module.notification.dto.NotificationDto;
import com.hyundai.dms.module.notification.service.NotificationService;
import com.hyundai.dms.module.user.entity.Branch;
import com.hyundai.dms.module.user.entity.User;
import com.hyundai.dms.module.user.repository.BranchRepository;
import com.hyundai.dms.module.user.repository.UserRepository;
import com.hyundai.dms.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import com.querydsl.core.types.Predicate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockTransferService {

    private final StockTransferRepository stockTransferRepository;
    private final VehicleRepository vehicleRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final StockTransferMapper transferMapper;
    private final StockStatusTransitionValidator transitionValidator;
    private final NotificationService notificationService;
    private final QueryDslPredicateBuilder<StockTransfer> predicateBuilder = new QueryDslPredicateBuilder<>(StockTransfer.class);

    @LogExecution
    @Audited(entity = "StockTransfer", action = ActionType.CREATE)
    @Transactional
    public StockTransferDto requestTransfer(RequestTransferRequest request) {
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", request.getVehicleId()));

        if (vehicle.getStatus() != StockStatus.AVAILABLE) {
            throw new BusinessRuleException("Vehicle must be AVAILABLE to request transfer. Current status: " + vehicle.getStatus());
        }

        Branch toBranch = branchRepository.findById(request.getToBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", request.getToBranchId()));

        if (vehicle.getBranch().getId().equals(toBranch.getId())) {
            throw new BusinessRuleException("Cannot transfer vehicle to the same branch");
        }

        User currentUser = getCurrentUser();

        StockTransfer transfer = StockTransfer.builder()
                .vehicle(vehicle)
                .fromBranch(vehicle.getBranch())
                .toBranch(toBranch)
                .requestedBy(currentUser)
                .status(TransferStatus.PENDING)
                .requestDate(LocalDateTime.now())
                .remarks(request.getRemarks())
                .build();

        transfer = stockTransferRepository.save(transfer);
        log.info("Stock transfer requested: vehicle VIN={} from {} to {}", vehicle.getVin(),
                vehicle.getBranch().getName(), toBranch.getName());
        return transferMapper.toDto(transfer);
    }

    @LogExecution
    @Audited(entity = "StockTransfer", action = ActionType.UPDATE)
    @PreAuthorize("hasRole('MANAGER_VIEWER') or hasRole('SUPER_ADMIN')")
    @Transactional
    public StockTransferDto approveTransfer(Long transferId) {
        StockTransfer transfer = findTransferOrThrow(transferId);

        if (transfer.getStatus() != TransferStatus.PENDING) {
            throw new BusinessRuleException("Only PENDING transfers can be approved");
        }

        User currentUser = getCurrentUser();
        transfer.setStatus(TransferStatus.APPROVED);
        transfer.setApprovedBy(currentUser);
        transfer.setApprovalDate(LocalDateTime.now());
        transfer = stockTransferRepository.save(transfer);

        // Notify the requesting user about approval
        if (transfer.getRequestedBy() != null) {
            notificationService.sendToUser(transfer.getRequestedBy().getId(), NotificationDto.builder()
                    .title("Stock Transfer Approved")
                    .message("Your transfer request for vehicle VIN=" + transfer.getVehicle().getVin()
                            + " has been approved")
                    .module("INVENTORY")
                    .priority("HIGH")
                    .deepLink("/inventory/transfers/" + transferId)
                    .build());
        }

        log.info("Stock transfer {} approved", transferId);
        return transferMapper.toDto(transfer);
    }

    @LogExecution
    @Audited(entity = "StockTransfer", action = ActionType.UPDATE)
    @PreAuthorize("hasRole('MANAGER_VIEWER') or hasRole('SUPER_ADMIN')")
    @Transactional
    public StockTransferDto rejectTransfer(Long transferId, String remarks) {
        StockTransfer transfer = findTransferOrThrow(transferId);

        if (transfer.getStatus() != TransferStatus.PENDING) {
            throw new BusinessRuleException("Only PENDING transfers can be rejected");
        }

        transfer.setStatus(TransferStatus.REJECTED);
        transfer.setApprovalDate(LocalDateTime.now());
        if (remarks != null && !remarks.isBlank()) {
            transfer.setRemarks(remarks);
        }
        transfer = stockTransferRepository.save(transfer);

        log.info("Stock transfer {} rejected", transferId);
        return transferMapper.toDto(transfer);
    }

    @LogExecution
    @Audited(entity = "StockTransfer", action = ActionType.UPDATE)
    @Transactional
    public StockTransferDto completeTransfer(Long transferId) {
        StockTransfer transfer = findTransferOrThrow(transferId);

        if (transfer.getStatus() != TransferStatus.APPROVED) {
            throw new BusinessRuleException("Only APPROVED transfers can be completed");
        }

        // Move vehicle to target branch and set TRANSFERRED status
        Vehicle vehicle = transfer.getVehicle();
        transitionValidator.validate(vehicle.getStatus(), StockStatus.TRANSFERRED);
        vehicle.setStatus(StockStatus.TRANSFERRED);
        vehicle.setBranch(transfer.getToBranch());
        vehicleRepository.save(vehicle);

        transfer.setStatus(TransferStatus.COMPLETED);
        transfer = stockTransferRepository.save(transfer);

        log.info("Stock transfer {} completed. Vehicle {} moved to branch {}",
                transferId, vehicle.getVin(), transfer.getToBranch().getName());
        return transferMapper.toDto(transfer);
    }

    @LogExecution
    @Transactional(readOnly = true)
    public PageResponse<StockTransferDto> listTransfers(FilterRequest filterRequest) {
        Predicate predicate = predicateBuilder.build(filterRequest.filters());
        PageRequest pageRequest = PageUtils.buildPageRequest(
                filterRequest.page(), filterRequest.size(), filterRequest.sorts());
        Page<StockTransfer> page = stockTransferRepository.findAll(predicate, pageRequest);
        Page<StockTransferDto> dtoPage = page.map(transferMapper::toDto);
        return PageUtils.toPageResponse(dtoPage);
    }

    private StockTransfer findTransferOrThrow(Long id) {
        return stockTransferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockTransfer", id));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userRepository.findById(userDetails.getId()).orElse(null);
        }
        return null;
    }
}
