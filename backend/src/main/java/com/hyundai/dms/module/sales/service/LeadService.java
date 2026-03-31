package com.hyundai.dms.module.sales.service;

import com.hyundai.dms.audit.Audited;
import com.hyundai.dms.common.PageResponse;
import com.hyundai.dms.common.PageUtils;
import com.hyundai.dms.common.enums.ActionType;
import com.hyundai.dms.common.filter.FilterRequest;
import com.hyundai.dms.common.filter.QueryDslPredicateBuilder;
import com.hyundai.dms.common.logging.LogExecution;
import com.hyundai.dms.exception.BusinessRuleException;
import com.hyundai.dms.exception.ResourceNotFoundException;
import com.hyundai.dms.module.inventory.entity.Vehicle;
import com.hyundai.dms.module.inventory.repository.VehicleRepository;
import com.hyundai.dms.module.sales.dto.CreateLeadRequest;
import com.hyundai.dms.module.sales.dto.LeadDto;
import com.hyundai.dms.module.sales.dto.StageTransitionRequest;
import com.hyundai.dms.module.sales.entity.Customer;
import com.hyundai.dms.module.sales.entity.Lead;
import com.hyundai.dms.module.sales.enums.LeadSource;
import com.hyundai.dms.module.sales.enums.LeadStage;
import com.hyundai.dms.module.sales.mapper.LeadMapper;
import com.hyundai.dms.module.sales.repository.CustomerRepository;
import com.hyundai.dms.module.sales.repository.LeadRepository;
import com.hyundai.dms.module.sales.validator.LeadStageTransitionValidator;
import com.hyundai.dms.module.user.entity.Branch;
import com.hyundai.dms.module.user.entity.User;
import com.hyundai.dms.module.user.repository.BranchRepository;
import com.hyundai.dms.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import com.querydsl.core.types.Predicate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class LeadService {

    private final LeadRepository leadRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final VehicleRepository vehicleRepository;
    private final LeadMapper leadMapper;
    private final LeadStageTransitionValidator stageValidator;
    private final QueryDslPredicateBuilder<Lead> predicateBuilder = new QueryDslPredicateBuilder<>(Lead.class);



    @LogExecution
    @Transactional(readOnly = true)
    public PageResponse<LeadDto> listLeads(FilterRequest filterRequest) {
        Predicate predicate = predicateBuilder.build(filterRequest.filters());
        PageRequest pageRequest = PageUtils.buildPageRequest(
                filterRequest.page(), filterRequest.size(), filterRequest.sorts());
        Page<Lead> page = leadRepository.findAll(predicate, pageRequest);
        Page<LeadDto> dtoPage = page.map(leadMapper::toDto);
        return PageUtils.toPageResponse(dtoPage);
    }

    @LogExecution
    @Transactional(readOnly = true)
    public LeadDto getLeadById(Long id) {
        Lead lead = findLeadOrThrow(id);
        return leadMapper.toDto(lead);
    }

    @LogExecution
    @Audited(entity = "Lead", action = ActionType.CREATE)
    @PreAuthorize("hasRole('SALES_CRM_EXEC') or hasRole('SUPER_ADMIN')")
    @Transactional
    public LeadDto createLead(CreateLeadRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.getCustomerId()));

        // Auto-assign from authenticated user if not provided
        User currentUser = getCurrentUser();

        Long assignedToId = request.getAssignedToId() != null ? request.getAssignedToId() : currentUser.getId();
        User assignedTo = userRepository.findById(assignedToId)
                .orElseThrow(() -> new ResourceNotFoundException("User", assignedToId));

        Long branchId = request.getBranchId() != null ? request.getBranchId()
                : (currentUser.getBranch() != null ? currentUser.getBranch().getId() : null);
        if (branchId == null) {
            throw new BusinessRuleException("Branch ID is required — your account has no default branch assigned");
        }
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", branchId));

        Vehicle vehicle = null;
        if (request.getVehicleId() != null) {
            vehicle = vehicleRepository.findById(request.getVehicleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle", request.getVehicleId()));
        }

        Lead lead = Lead.builder()
                .customer(customer)
                .assignedTo(assignedTo)
                .modelInterested(request.getModelInterested())
                .source(LeadSource.valueOf(request.getSource().toUpperCase()))
                .stage(LeadStage.NEW_LEAD)
                .vehicle(vehicle)
                .branch(branch)
                .build();

        lead = leadRepository.save(lead);
        log.info("Created lead id={} for customer={}", lead.getId(), customer.getMobile());

        return leadMapper.toDto(lead);

    }

    @LogExecution
    @Audited(entity = "Lead", action = ActionType.UPDATE)
    @PreAuthorize("hasRole('SALES_CRM_EXEC') or hasRole('SUPER_ADMIN')")
    @Transactional
    public LeadDto transitionStage(Long id, StageTransitionRequest request) {
        Lead lead = findLeadOrThrow(id);
        LeadStage newStage = LeadStage.valueOf(request.getNewStage().toUpperCase());

        stageValidator.validate(lead.getStage(), newStage);

        LeadStage oldStage = lead.getStage();
        lead.setStage(newStage);

        if (newStage == LeadStage.LOST) {
            if (request.getLostReason() == null || request.getLostReason().isBlank()) {
                throw new BusinessRuleException("Lost reason is required when marking a lead as LOST");
            }
            lead.setLostReason(request.getLostReason());
        }

        lead = leadRepository.save(lead);
        log.info("Lead {} stage: {} → {}", id, oldStage, newStage);

        return leadMapper.toDto(lead);

    }

    @LogExecution
    @Audited(entity = "Lead", action = ActionType.DELETE)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Transactional
    public void deleteLead(Long id) {
        Lead lead = findLeadOrThrow(id);
        leadRepository.delete(lead);
        log.info("Deleted lead id={}", id);
    }

    /**
     * Package-visible helper used by QuotationService and BookingService.
     */
    public Lead findLeadOrThrow(Long id) {
        return leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead", id));
    }

    private User getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof com.hyundai.dms.security.CustomUserDetails userDetails) {
            return userDetails.getUser();
        }
        throw new BusinessRuleException("Unable to determine current user");
    }
}
