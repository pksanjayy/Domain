package com.hyundai.dms.module.inventory.repository;

import com.hyundai.dms.module.inventory.entity.GrnRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GrnRecordRepository extends JpaRepository<GrnRecord, Long>, JpaSpecificationExecutor<GrnRecord> {

    Optional<GrnRecord> findByVehicleId(Long vehicleId);

    Optional<GrnRecord> findByGrnNumber(String grnNumber);

    boolean existsByGrnNumber(String grnNumber);
}
