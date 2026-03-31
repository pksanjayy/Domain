package com.hyundai.dms.module.sales.repository;

import com.hyundai.dms.module.sales.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>, QuerydslPredicateExecutor<Customer> {

    Optional<Customer> findByMobile(String mobile);

    boolean existsByMobile(String mobile);
}
