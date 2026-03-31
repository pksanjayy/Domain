package com.hyundai.dms.module.inventory.repository;

import com.hyundai.dms.module.inventory.entity.StockTransfer;
import com.hyundai.dms.module.inventory.enums.TransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockTransferRepository extends JpaRepository<StockTransfer, Long>, QuerydslPredicateExecutor<StockTransfer> {

    List<StockTransfer> findByVehicleId(Long vehicleId);

    List<StockTransfer> findByStatus(TransferStatus status);
}
