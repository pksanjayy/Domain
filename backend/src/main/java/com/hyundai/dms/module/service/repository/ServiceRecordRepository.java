package com.hyundai.dms.module.service.repository;

import com.hyundai.dms.module.service.entity.ServiceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRecordRepository extends JpaRepository<ServiceRecord, Long> {
    List<ServiceRecord> findByBranchId(Long branchId);
    boolean existsByServiceBookingId(Long bookingId);
}
