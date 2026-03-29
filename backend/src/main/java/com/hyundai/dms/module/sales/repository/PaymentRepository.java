package com.hyundai.dms.module.sales.repository;

import com.hyundai.dms.module.sales.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {
    List<Payment> findByCustomerId(Long customerId);
    List<Payment> findByCustomerBranchId(Long branchId);
}
