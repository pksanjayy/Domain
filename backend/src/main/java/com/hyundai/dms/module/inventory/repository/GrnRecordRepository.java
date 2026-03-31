package com.hyundai.dms.module.inventory.repository;

import com.hyundai.dms.module.inventory.entity.GrnRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GrnRecordRepository extends JpaRepository<GrnRecord, Long>, QuerydslPredicateExecutor<GrnRecord> {

    Optional<GrnRecord> findByVehicleId(Long vehicleId);

    Optional<GrnRecord> findByGrnNumber(String grnNumber);

    boolean existsByGrnNumber(String grnNumber);
}
