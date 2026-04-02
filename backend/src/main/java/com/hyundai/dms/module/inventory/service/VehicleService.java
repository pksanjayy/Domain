package com.hyundai.dms.module.inventory.service;

import com.hyundai.dms.audit.Audited;
import com.hyundai.dms.common.PageResponse;
import com.hyundai.dms.common.PageUtils;
import com.hyundai.dms.common.enums.ActionType;
import com.hyundai.dms.common.enums.RoleName;
import com.hyundai.dms.common.filter.FilterRequest;
import com.hyundai.dms.common.filter.QueryDslPredicateBuilder;
import com.hyundai.dms.common.logging.LogExecution;
import com.hyundai.dms.exception.BusinessRuleException;
import com.hyundai.dms.exception.DuplicateResourceException;
import com.hyundai.dms.exception.ResourceNotFoundException;
import com.hyundai.dms.module.inventory.dto.*;
import com.hyundai.dms.module.inventory.entity.Vehicle;
import com.hyundai.dms.module.inventory.enums.FuelType;
import com.hyundai.dms.module.inventory.enums.StockStatus;
import com.hyundai.dms.module.inventory.enums.TransmissionType;
import com.hyundai.dms.module.inventory.mapper.VehicleMapper;
import com.hyundai.dms.module.inventory.repository.VehicleRepository;
import com.hyundai.dms.module.inventory.validator.StockStatusTransitionValidator;
import com.hyundai.dms.module.notification.dto.NotificationDto;
import com.hyundai.dms.module.notification.service.NotificationService;
import com.hyundai.dms.module.user.entity.Branch;
import com.hyundai.dms.module.user.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import com.querydsl.core.types.Predicate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final BranchRepository branchRepository;
    private final VehicleMapper vehicleMapper;
    private final StockStatusTransitionValidator transitionValidator;
    private final NotificationService notificationService;
    private final QueryDslPredicateBuilder<Vehicle> predicateBuilder = new QueryDslPredicateBuilder<>(Vehicle.class);

    // ── Reads ──

    @LogExecution
    @Transactional(readOnly = true)
    public PageResponse<VehicleListDto> listVehicles(FilterRequest filterRequest) {
        List<com.hyundai.dms.common.filter.FilterCriteria> filters = new ArrayList<>(filterRequest.filters());
        
        // Always exclude DELETED vehicles if not explicitly requested
        boolean hasStatusFilter = filters.stream().anyMatch(f -> f.field().equals("status"));
        if (!hasStatusFilter) {
            filters.add(new com.hyundai.dms.common.filter.FilterCriteria("status", "neq", "DELETED"));
        }

        Predicate predicate = predicateBuilder.build(filters);
        PageRequest pageRequest = PageUtils.buildPageRequest(
                filterRequest.page(), filterRequest.size(), filterRequest.sorts());
        Page<Vehicle> page = vehicleRepository.findAll(predicate, pageRequest);
        Page<VehicleListDto> dtoPage = page.map(vehicleMapper::toListDto);
        return PageUtils.toPageResponse(dtoPage);
    }

    @LogExecution
    @Transactional(readOnly = true)
    public VehicleDetailDto getVehicleById(Long id) {
        Vehicle vehicle = findVehicleOrThrow(id);
        return vehicleMapper.toDetailDto(vehicle);
    }

    @LogExecution
    @Transactional(readOnly = true)
    public VehicleDetailDto getVehicleByVin(String vin) {
        Vehicle vehicle = vehicleRepository.findByVin(vin)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "vin", vin));
        return vehicleMapper.toDetailDto(vehicle);
    }

    @LogExecution
    @Cacheable(value = "vehicleModels")
    @Transactional(readOnly = true)
    public List<Map<String, String>> getAvailableModels() {
        return vehicleRepository.findAvailableModels().stream()
                .map(row -> Map.of("brand", (String) row[0], "model", (String) row[1]))
                .collect(Collectors.toList());
    }

    @LogExecution
    @Transactional(readOnly = true)
    public DashboardSummaryDto getDashboardSummary() {
        // Status counts
        List<Object[]> statusCounts = vehicleRepository.countGroupedByStatus();
        Map<String, Long> statusMap = new HashMap<>();
        long totalStock = 0;
        for (Object[] row : statusCounts) {
            StockStatus status = (StockStatus) row[0];
            Long count = (Long) row[1];
            statusMap.put(status.name(), count);
            totalStock += count;
        }

        // Ageing buckets
        List<Object[]> ageingData = vehicleRepository.findAgeingSummary();
        List<AgeingBucketDto> buckets = new ArrayList<>();
        Map<String, String> severityMap = Map.of(
                "0-30", "green", "31-60", "amber", "61-90", "orange", "90+", "red"
        );
        for (Object[] row : ageingData) {
            String range = (String) row[0];
            Long count = (Long) row[1];
            buckets.add(AgeingBucketDto.builder()
                    .range(range)
                    .count(count)
                    .severity(severityMap.getOrDefault(range, "green"))
                    .build());
        }

        // Ensure all 4 buckets present
        for (String range : List.of("0-30", "31-60", "61-90", "90+")) {
            if (buckets.stream().noneMatch(b -> b.getRange().equals(range))) {
                buckets.add(AgeingBucketDto.builder()
                        .range(range).count(0).severity(severityMap.get(range)).build());
            }
        }
        buckets.sort(Comparator.comparing(AgeingBucketDto::getRange));

        // Branch breakdown
        List<Branch> branches = branchRepository.findAll();
        Map<String, Map<String, Long>> branchBreakdown = new LinkedHashMap<>();
        for (Branch branch : branches) {
            List<Object[]> branchCounts = vehicleRepository.countByBranchGroupedByStatus(branch.getId());
            Map<String, Long> counts = new HashMap<>();
            for (Object[] row : branchCounts) {
                counts.put(((StockStatus) row[0]).name(), (Long) row[1]);
            }
            branchBreakdown.put(branch.getName(), counts);
        }

        // Branch distribution (total count per branch)
        List<DashboardSummaryDto.BranchDistributionDto> branchDistribution = new ArrayList<>();
        for (Map.Entry<String, Map<String, Long>> entry : branchBreakdown.entrySet()) {
            long total = entry.getValue().values().stream().mapToLong(Long::longValue).sum();
            branchDistribution.add(DashboardSummaryDto.BranchDistributionDto.builder()
                    .branchName(entry.getKey())
                    .count(total)
                    .build());
        }

        return DashboardSummaryDto.builder()
                .totalStock(totalStock)
                .available(statusMap.getOrDefault("AVAILABLE", 0L))
                .onHold(statusMap.getOrDefault("HOLD", 0L))
                .booked(statusMap.getOrDefault("BOOKED", 0L))
                .ageingBuckets(buckets)
                .statusBreakdown(statusMap)
                .branchDistribution(branchDistribution)
                .branchBreakdown(branchBreakdown)
                .build();
    }

    @LogExecution
    @Transactional(readOnly = true)
    public List<VehicleListDto> searchByVin(String vin) {
        return vehicleRepository.findByVinContainingIgnoreCase(vin).stream()
                .filter(v -> v.getStatus() != StockStatus.DELETED)
                .map(vehicleMapper::toListDto)
                .collect(Collectors.toList());
    }

    @LogExecution
    @Transactional(readOnly = true)
    public byte[] exportVehiclesCsv() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("VIN,Brand,Model,Variant,Colour,Status,FuelType,Transmission,MSRP,Branch\n");
        for (Vehicle v : vehicles) {
            sb.append(String.join(",",
                    v.getVin(),
                    v.getBrand(),
                    v.getModel(),
                    v.getVariant() != null ? v.getVariant() : "",
                    v.getColour() != null ? v.getColour() : "",
                    v.getStatus() != null ? v.getStatus().name() : "",
                    v.getFuelType() != null ? v.getFuelType().name() : "",
                    v.getTransmission() != null ? v.getTransmission().name() : "",
                    v.getMsrp() != null ? v.getMsrp().toString() : "",
                    v.getBranch() != null ? v.getBranch().getName() : ""
            )).append("\n");
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    // ── Writes ──

    @LogExecution
    @Audited(entity = "Vehicle", action = ActionType.CREATE)
    @CacheEvict(value = "vehicleModels", allEntries = true)
    @PreAuthorize("hasRole('WORKSHOP_EXEC') or hasRole('SUPER_ADMIN')")
    @Transactional
    public VehicleDetailDto createVehicle(CreateVehicleRequest request) {
        String correlationId = MDC.get("correlationId");

        if (vehicleRepository.existsByVin(request.getVin())) {
            throw new DuplicateResourceException("Vehicle", "vin", request.getVin());
        }

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", request.getBranchId()));

        Vehicle vehicle = Vehicle.builder()
                .vin(request.getVin().toUpperCase())
                .brand(request.getBrand())
                .model(request.getModel())
                .variant(request.getVariant())
                .colour(request.getColour())
                .fuelType(FuelType.valueOf(request.getFuelType().toUpperCase()))
                .transmission(TransmissionType.valueOf(request.getTransmission().toUpperCase()))
                .manufacturedDate(request.getManufacturedDate())
                .msrp(request.getMsrp())
                .engineNumber(request.getEngineNumber())
                .chassisNumber(request.getChassisNumber())
                .keyNumber(request.getKeyNumber())
                .exteriorColourCode(request.getExteriorColourCode())
                .interiorColourCode(request.getInteriorColourCode())
                .status(StockStatus.IN_TRANSIT)
                .branch(branch)
                .ageDays(0)
                .build();

        vehicle = vehicleRepository.save(vehicle);
        log.info("[{}] Created vehicle: VIN={}", correlationId, vehicle.getVin());
        return vehicleMapper.toDetailDto(vehicle);
    }

    @LogExecution
    @Audited(entity = "Vehicle", action = ActionType.UPDATE)
    @CacheEvict(value = "vehicleModels", allEntries = true)
    @Transactional
    public VehicleDetailDto updateVehicle(Long id, UpdateVehicleRequest request) {
        String correlationId = MDC.get("correlationId");

        Vehicle vehicle = findVehicleOrThrow(id);

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", request.getBranchId()));

        vehicle.setVin(request.getVin().toUpperCase());
        vehicle.setBrand(request.getBrand());
        vehicle.setModel(request.getModel());
        vehicle.setVariant(request.getVariant());
        vehicle.setColour(request.getColour());
        vehicle.setFuelType(FuelType.valueOf(request.getFuelType().toUpperCase()));
        vehicle.setTransmission(TransmissionType.valueOf(request.getTransmission().toUpperCase()));
        vehicle.setManufacturedDate(request.getManufacturedDate());
        vehicle.setMsrp(request.getMsrp());
        vehicle.setEngineNumber(request.getEngineNumber());
        vehicle.setChassisNumber(request.getChassisNumber());
        vehicle.setKeyNumber(request.getKeyNumber());
        vehicle.setExteriorColourCode(request.getExteriorColourCode());
        vehicle.setInteriorColourCode(request.getInteriorColourCode());
        vehicle.setBranch(branch);

        try {
            vehicle = vehicleRepository.save(vehicle);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new BusinessRuleException("Vehicle was modified concurrently, please refresh");
        }

        log.info("[{}] Updated vehicle: id={}", correlationId, id);
        return vehicleMapper.toDetailDto(vehicle);
    }

    @LogExecution
    @Audited(entity = "Vehicle", action = ActionType.UPDATE)
    @CacheEvict(value = "vehicleModels", allEntries = true)
    @Transactional
    public VehicleDetailDto transitionStatus(Long id, StatusTransitionRequest request) {
        String correlationId = MDC.get("correlationId");

        Vehicle vehicle = findVehicleOrThrow(id);
        StockStatus newStatus = StockStatus.valueOf(request.getNewStatus().toUpperCase());

        transitionValidator.validate(vehicle.getStatus(), newStatus);

        StockStatus oldStatus = vehicle.getStatus();
        vehicle.setStatus(newStatus);

        try {
            vehicle = vehicleRepository.save(vehicle);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new BusinessRuleException("Vehicle was modified concurrently, please refresh");
        }

        log.info("[{}] Vehicle {} status: {} → {}", correlationId, vehicle.getVin(), oldStatus, newStatus);

        // Notify SALES_CRM_EXEC when vehicle becomes AVAILABLE
        if (newStatus == StockStatus.AVAILABLE) {
            notificationService.sendToRole(RoleName.SALES_CRM_EXEC, NotificationDto.builder()
                    .title("Vehicle Available")
                    .message("Vehicle " + vehicle.getBrand() + " " + vehicle.getModel()
                            + " (VIN=" + vehicle.getVin() + ") is now AVAILABLE at "
                            + vehicle.getBranch().getName())
                    .module("INVENTORY")
                    .priority("MEDIUM")
                    .deepLink("/inventory/vehicles/" + vehicle.getId())
                    .build(), null);
        }

        return vehicleMapper.toDetailDto(vehicle);
    }

    @LogExecution
    @Audited(entity = "Vehicle", action = ActionType.UPDATE)
    @CacheEvict(value = "vehicleModels", allEntries = true)
    @PreAuthorize("hasRole('SALES_CRM_EXEC') or hasRole('SUPER_ADMIN')")
    @Transactional
    public VehicleDetailDto holdVehicle(Long id, String remarks) {
        Vehicle vehicle = findVehicleOrThrow(id);
        transitionValidator.validate(vehicle.getStatus(), StockStatus.HOLD);
        vehicle.setStatus(StockStatus.HOLD);

        try {
            vehicle = vehicleRepository.save(vehicle);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new BusinessRuleException("Vehicle was modified concurrently, please refresh");
        }

        log.info("Vehicle {} put on HOLD", vehicle.getVin());
        return vehicleMapper.toDetailDto(vehicle);
    }

    @LogExecution
    @Audited(entity = "Vehicle", action = ActionType.DELETE)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Transactional
    public void deleteVehicle(Long id) {
        Vehicle vehicle = findVehicleOrThrow(id);
        vehicle.setStatus(StockStatus.DELETED);
        vehicleRepository.save(vehicle);
        log.info("Soft-deleted vehicle id={} (Status set to DELETED)", id);
    }

    // ── Helpers ──

    private Vehicle findVehicleOrThrow(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", id));
    }
}
