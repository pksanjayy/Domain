package com.hyundai.dms.module.inventory.repository;

import com.hyundai.dms.module.inventory.entity.VehicleAccessory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleAccessoryRepository extends JpaRepository<VehicleAccessory, Long> {

    List<VehicleAccessory> findByVehicleId(Long vehicleId);
}
