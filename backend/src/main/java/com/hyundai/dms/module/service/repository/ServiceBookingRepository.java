package com.hyundai.dms.module.service.repository;

import com.hyundai.dms.module.service.entity.ServiceBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceBookingRepository extends JpaRepository<ServiceBooking, Long> {
    List<ServiceBooking> findByBranchId(Long branchId);
    boolean existsByBookingId(String bookingId);
}
