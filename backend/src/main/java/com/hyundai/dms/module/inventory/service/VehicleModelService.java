package com.hyundai.dms.module.inventory.service;

import com.hyundai.dms.module.inventory.dto.VehicleModelDto;
import com.hyundai.dms.module.inventory.entity.VehicleModel;
import com.hyundai.dms.module.inventory.mapper.VehicleModelMapper;
import com.hyundai.dms.module.inventory.repository.VehicleModelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleModelService {

    private final VehicleModelRepository vehicleModelRepository;
    private final VehicleModelMapper vehicleModelMapper;

    /**
     * Get or create a VehicleModel for the given brand and model.
     * Increments vehicle count when a new vehicle is added.
     */
    @Transactional
    public VehicleModel getOrCreateVehicleModel(String brand, String model) {
        return vehicleModelRepository.findByBrandAndModel(brand, model)
                .map(vm -> {
                    vm.setVehicleCount(vm.getVehicleCount() + 1);
                    return vehicleModelRepository.save(vm);
                })
                .orElseGet(() -> {
                    VehicleModel newModel = VehicleModel.builder()
                            .brand(brand)
                            .model(model)
                            .isActive(true)
                            .vehicleCount(1)
                            .build();
                    log.info("Created new vehicle model: {} {}", brand, model);
                    return vehicleModelRepository.save(newModel);
                });
    }

    /**
     * Decrement vehicle count when a vehicle is deleted.
     * Deactivates the model if count reaches 0.
     */
    @Transactional
    public void decrementVehicleCount(String brand, String model) {
        vehicleModelRepository.findByBrandAndModel(brand, model)
                .ifPresent(vm -> {
                    int newCount = Math.max(0, vm.getVehicleCount() - 1);
                    vm.setVehicleCount(newCount);
                    if (newCount == 0) {
                        vm.setIsActive(false);
                        log.info("Deactivated vehicle model: {} {} (no vehicles remaining)", brand, model);
                    }
                    vehicleModelRepository.save(vm);
                });
    }

    /**
     * Update vehicle model when brand/model changes.
     * Decrements old model, increments new model.
     */
    @Transactional
    public VehicleModel updateVehicleModel(String oldBrand, String oldModel, String newBrand, String newModel) {
        // If brand/model unchanged, return existing
        if (oldBrand.equals(newBrand) && oldModel.equals(newModel)) {
            return vehicleModelRepository.findByBrandAndModel(oldBrand, oldModel)
                    .orElseThrow(() -> new IllegalStateException("VehicleModel not found"));
        }

        // Decrement old model
        decrementVehicleCount(oldBrand, oldModel);

        // Get or create new model
        return getOrCreateVehicleModel(newBrand, newModel);
    }

    /**
     * Get all active vehicle models.
     */
    @Transactional(readOnly = true)
    public List<VehicleModel> getAllActiveModels() {
        return vehicleModelRepository.findByIsActiveTrue();
    }

    /**
     * Get all active vehicle models as DTOs.
     */
    @Transactional(readOnly = true)
    public List<VehicleModelDto> getAllActiveModelsDto() {
        return vehicleModelMapper.toDtoList(vehicleModelRepository.findByIsActiveTrue());
    }

    /**
     * Get all vehicle models (including inactive).
     */
    @Transactional(readOnly = true)
    public List<VehicleModel> getAllModels() {
        return vehicleModelRepository.findAllByOrderByBrandAscModelAsc();
    }

    /**
     * Get all vehicle models as DTOs (including inactive).
     */
    @Transactional(readOnly = true)
    public List<VehicleModelDto> getAllModelsDto() {
        return vehicleModelMapper.toDtoList(vehicleModelRepository.findAllByOrderByBrandAscModelAsc());
    }
}
