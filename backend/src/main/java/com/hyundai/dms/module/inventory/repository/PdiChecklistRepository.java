package com.hyundai.dms.module.inventory.repository;

import com.hyundai.dms.module.inventory.entity.PdiChecklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PdiChecklistRepository extends JpaRepository<PdiChecklist, Long>, QuerydslPredicateExecutor<PdiChecklist> {

    Optional<PdiChecklist> findByVehicleId(Long vehicleId);

    boolean existsByVehicleId(Long vehicleId);
}
