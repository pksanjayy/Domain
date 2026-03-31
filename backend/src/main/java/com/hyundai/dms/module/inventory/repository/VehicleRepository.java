package com.hyundai.dms.module.inventory.repository;

import com.hyundai.dms.module.inventory.entity.Vehicle;
import com.hyundai.dms.module.inventory.enums.StockStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long>, QuerydslPredicateExecutor<Vehicle> {

    Optional<Vehicle> findByVin(String vin);

    List<Vehicle> findByVinContainingIgnoreCase(String vin);

    Page<Vehicle> findByBranchIdAndStatus(Long branchId, StockStatus status, Pageable pageable);

    long countByBranchIdAndStatus(Long branchId, StockStatus status);

    long countByStatus(StockStatus status);

    long countByBranchId(Long branchId);

    @Query("SELECT v.brand, v.model FROM Vehicle v WHERE v.status = 'AVAILABLE' AND v.status != 'DELETED' GROUP BY v.brand, v.model ORDER BY v.brand, v.model")
    List<Object[]> findAvailableModels();

    @Query("""
            SELECT
                CASE
                    WHEN v.ageDays BETWEEN 0 AND 30 THEN '0-30'
                    WHEN v.ageDays BETWEEN 31 AND 60 THEN '31-60'
                    WHEN v.ageDays BETWEEN 61 AND 90 THEN '61-90'
                    ELSE '90+'
                END AS bucket,
                COUNT(v)
            FROM Vehicle v
            WHERE v.status NOT IN (com.hyundai.dms.module.inventory.enums.StockStatus.INVOICED, com.hyundai.dms.module.inventory.enums.StockStatus.TRANSFERRED, com.hyundai.dms.module.inventory.enums.StockStatus.DELETED)
            GROUP BY
                CASE
                    WHEN v.ageDays BETWEEN 0 AND 30 THEN '0-30'
                    WHEN v.ageDays BETWEEN 31 AND 60 THEN '31-60'
                    WHEN v.ageDays BETWEEN 61 AND 90 THEN '61-90'
                    ELSE '90+'
                END
            """)
    List<Object[]> findAgeingSummary();

    @Modifying
    @Query(value = "UPDATE vehicles SET age_days = DATEDIFF(CURRENT_DATE, DATE(created_at)) WHERE status NOT IN ('INVOICED', 'TRANSFERRED', 'DELETED')", nativeQuery = true)
    int updateAgeDays();

    @Query("SELECT v FROM Vehicle v WHERE v.ageDays >= :threshold AND v.status NOT IN (com.hyundai.dms.module.inventory.enums.StockStatus.INVOICED, com.hyundai.dms.module.inventory.enums.StockStatus.TRANSFERRED, com.hyundai.dms.module.inventory.enums.StockStatus.DELETED)")
    List<Vehicle> findAgedVehicles(@Param("threshold") int threshold);

    boolean existsByVin(String vin);

    @Query("SELECT v.status, COUNT(v) FROM Vehicle v WHERE v.branch.id = :branchId AND v.status != 'DELETED' GROUP BY v.status")
    List<Object[]> countByBranchGroupedByStatus(@Param("branchId") Long branchId);

    @Query("SELECT v.status, COUNT(v) FROM Vehicle v WHERE v.status != 'DELETED' GROUP BY v.status")
    List<Object[]> countGroupedByStatus();
}
