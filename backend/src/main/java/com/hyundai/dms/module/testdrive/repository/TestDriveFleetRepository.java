package com.hyundai.dms.module.testdrive.repository;

import com.hyundai.dms.module.testdrive.entity.TestDriveFleet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TestDriveFleetRepository extends JpaRepository<TestDriveFleet, Long>, QuerydslPredicateExecutor<TestDriveFleet> {
    Optional<TestDriveFleet> findByVin(String vin);
    Optional<TestDriveFleet> findByFleetId(String fleetId);
    boolean existsByVin(String vin);
    boolean existsByFleetId(String fleetId);
}
