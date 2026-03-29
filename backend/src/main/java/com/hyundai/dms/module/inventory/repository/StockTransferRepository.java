package com.hyundai.dms.module.inventory.repository;

import com.hyundai.dms.module.inventory.entity.StockTransfer;
import com.hyundai.dms.module.inventory.enums.TransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockTransferRepository extends JpaRepository<StockTransfer, Long>, JpaSpecificationExecutor<StockTransfer> {

    List<StockTransfer> findByVehicleId(Long vehicleId);

    List<StockTransfer> findByStatus(TransferStatus status);
}
